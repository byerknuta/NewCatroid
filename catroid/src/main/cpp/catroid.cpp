#include <jni.h>
#include <map>
#include <string>
#include <mutex>
#include <vector>
#include <numeric>
#include <cmath>
#include <algorithm>
#include "earcut.hpp"
#include <android/native_window_jni.h>
#include <dlfcn.h>
#include <fstream>
#include <unwind.h>
#include <dlfcn.h>
#include <sstream>
#include <signal.h>
#include <cstring>
#include <spawn.h>
#include <unistd.h>
#include <sys/wait.h>
#include <thread>
#include <sys/socket.h>
#include <random>

#include "onnxruntime_cxx_api.h"

#include <android/log.h>

#include "newcatroid_gl_api.h"

struct CatroidTensor {
    std::string name;
    std::vector<int> shape;
    std::vector<float> data;
    std::vector<float> grad;
    int total_size;
    bool trainable;

    CatroidTensor(std::vector<int> s, float val, bool train, std::string n = "")
            : shape(s), trainable(train), name(n) {
        total_size = 1;
        for (int d : s) total_size *= d;
        data.assign(total_size, val);
        if (trainable) grad.assign(total_size, 0.0f);
    }


    void zero_grad() {
        if (trainable) std::fill(grad.begin(), grad.end(), 0.0f);
    }
};

struct OpNode {
    std::string op_type;
    std::shared_ptr<CatroidTensor> input_a;
    std::shared_ptr<CatroidTensor> input_b;
    std::shared_ptr<CatroidTensor> output;

    std::vector<int> meta_data;
};

struct AdamState {
    std::vector<float> m;
    std::vector<float> v;
    int t = 0;
};
static std::map<std::string, AdamState> g_AdamStates;

static std::map<std::string, std::shared_ptr<CatroidTensor>> g_Tensors;
static std::vector<OpNode> g_Tape;
static std::mutex g_MnnMutex;

static std::mt19937 rng(std::random_device{}());
static bool g_IsTraining = false;

void init_xavier(std::vector<float>& data, int fan_in, int fan_out) {
    float limit = sqrt(6.0f / (fan_in + fan_out));
    std::uniform_real_distribution<float> dist(-limit, limit);
    for (float &v : data) v = dist(rng);
}


void init_he(std::vector<float>& data, int fan_in) {
    float std_dev = sqrt(2.0f / fan_in);
    std::normal_distribution<float> dist(0.0f, std_dev);
    for (float &v : data) v = dist(rng);
}




void kernel_add_forward(const std::shared_ptr<CatroidTensor>& A, const std::shared_ptr<CatroidTensor>& B, std::shared_ptr<CatroidTensor>& C) {
    for (int i = 0; i < A->total_size; ++i) C->data[i] = A->data[i] + B->data[i % B->total_size];
}
void kernel_add_backward(OpNode& node) {
    if (node.input_a->trainable) for (int i = 0; i < node.output->total_size; ++i) node.input_a->grad[i] += node.output->grad[i];
    if (node.input_b->trainable) for (int i = 0; i < node.output->total_size; ++i) node.input_b->grad[i % node.input_b->total_size] += node.output->grad[i];
}

void kernel_sub_forward(const std::shared_ptr<CatroidTensor>& A, const std::shared_ptr<CatroidTensor>& B, std::shared_ptr<CatroidTensor>& C) {
    for (int i = 0; i < A->total_size; ++i) C->data[i] = A->data[i] - B->data[i % B->total_size];
}
void kernel_sub_backward(OpNode& node) {
    if (node.input_a->trainable) for (int i = 0; i < node.output->total_size; ++i) node.input_a->grad[i] += node.output->grad[i];
    if (node.input_b->trainable) for (int i = 0; i < node.output->total_size; ++i) node.input_b->grad[i % node.input_b->total_size] -= node.output->grad[i];
}

void kernel_mul_forward(const std::shared_ptr<CatroidTensor>& A, const std::shared_ptr<CatroidTensor>& B, std::shared_ptr<CatroidTensor>& C) {
    for (int i = 0; i < A->total_size; ++i) C->data[i] = A->data[i] * B->data[i % B->total_size];
}
void kernel_mul_backward(OpNode& node) {
    if (node.input_a->trainable) for (int i = 0; i < node.output->total_size; ++i) node.input_a->grad[i] += node.output->grad[i] * node.input_b->data[i % node.input_b->total_size];
    if (node.input_b->trainable) for (int i = 0; i < node.output->total_size; ++i) node.input_b->grad[i % node.input_b->total_size] += node.output->grad[i] * node.input_a->data[i];
}

void kernel_matmul_forward(const std::shared_ptr<CatroidTensor>& A, const std::shared_ptr<CatroidTensor>& B, std::shared_ptr<CatroidTensor>& C) {
    int M = A->shape[0], K = A->shape[1], N = B->shape[1];
    std::fill(C->data.begin(), C->data.end(), 0.0f);
    const float* a_ptr = A->data.data(); const float* b_ptr = B->data.data(); float* c_ptr = C->data.data();
    for (int i = 0; i < M; ++i) for (int k = 0; k < K; ++k) { float a_val = a_ptr[i * K + k]; for (int j = 0; j < N; ++j) c_ptr[i * N + j] += a_val * b_ptr[k * N + j]; }
}
void kernel_matmul_backward(OpNode& node) {
    auto A = node.input_a; auto B = node.input_b; auto C = node.output;
    int M = A->shape[0], K = A->shape[1], N = B->shape[1];
    if (A->trainable) for (int i = 0; i < M; ++i) for (int j = 0; j < N; ++j) { float grad_val = C->grad[i * N + j]; for (int k = 0; k < K; ++k) A->grad[i * K + k] += grad_val * B->data[k * N + j]; }
    if (B->trainable) for (int k = 0; k < K; ++k) for (int i = 0; i < M; ++i) { float a_val = A->data[i * K + k]; for (int j = 0; j < N; ++j) B->grad[k * N + j] += a_val * C->grad[i * N + j]; }
}


void kernel_relu_forward(const std::shared_ptr<CatroidTensor>& A, std::shared_ptr<CatroidTensor>& C) { for (int i = 0; i < A->total_size; ++i) C->data[i] = std::max(0.0f, A->data[i]); }
void kernel_relu_backward(OpNode& node) { if (!node.input_a->trainable) return; for (int i = 0; i < node.output->total_size; ++i) if (node.input_a->data[i] > 0) node.input_a->grad[i] += node.output->grad[i]; }

void kernel_sigmoid_forward(const std::shared_ptr<CatroidTensor>& A, std::shared_ptr<CatroidTensor>& C) { for (int i = 0; i < A->total_size; ++i) C->data[i] = 1.0f / (1.0f + std::exp(-A->data[i])); }
void kernel_sigmoid_backward(OpNode& node) { if (!node.input_a->trainable) return; for (int i = 0; i < node.output->total_size; ++i) { float val = node.output->data[i]; node.input_a->grad[i] += node.output->grad[i] * val * (1.0f - val); } }

void kernel_tanh_forward(const std::shared_ptr<CatroidTensor>& A, std::shared_ptr<CatroidTensor>& C) { for (int i = 0; i < A->total_size; ++i) C->data[i] = std::tanh(A->data[i]); }
void kernel_tanh_backward(OpNode& node) { if (!node.input_a->trainable) return; for (int i = 0; i < node.output->total_size; ++i) { float val = node.output->data[i]; node.input_a->grad[i] += node.output->grad[i] * (1.0f - val * val); } }

void kernel_softmax_forward(const std::shared_ptr<CatroidTensor>& A, std::shared_ptr<CatroidTensor>& C) {
    int rows = A->shape.size() > 1 ? A->shape[0] : 1;
    int cols = A->shape.back();
    for (int i = 0; i < rows; ++i) {
        float max_val = -1e9f; int offset = i * cols;
        for (int j = 0; j < cols; ++j) max_val = std::max(max_val, A->data[offset + j]);
        float sum = 0.0f;
        for (int j = 0; j < cols; ++j) { float val = std::exp(A->data[offset + j] - max_val); C->data[offset + j] = val; sum += val; }
        for (int j = 0; j < cols; ++j) C->data[offset + j] /= sum;
    }
}
void kernel_softmax_backward(OpNode& node) {
    auto S = node.output; auto A = node.input_a; if (!A->trainable) return;
    int rows = A->shape.size() > 1 ? A->shape[0] : 1; int cols = A->shape.back();



    for(int i=0; i < S->total_size; ++i) A->grad[i] += S->grad[i];
}



void kernel_mse_loss_forward(const std::shared_ptr<CatroidTensor>& Pred, const std::shared_ptr<CatroidTensor>& Target, std::shared_ptr<CatroidTensor>& Loss) {
    float total_loss = 0.0f;
    for(int i=0; i<Pred->total_size; i++) { float diff = Pred->data[i] - Target->data[i]; total_loss += diff * diff; }
    Loss->data[0] = total_loss / Pred->total_size;
}
void kernel_mse_loss_backward(OpNode& node) {
    auto Pred = node.input_a; auto Target = node.input_b;
    if (!Pred->trainable) return;
    float scale = 2.0f / Pred->total_size;
    for(int i=0; i < Pred->total_size; i++) Pred->grad[i] += scale * (Pred->data[i] - Target->data[i]);
}


void kernel_sum_forward(const std::shared_ptr<CatroidTensor>& A, std::shared_ptr<CatroidTensor>& C) {
    C->data[0] = std::accumulate(A->data.begin(), A->data.end(), 0.0f);
}
void kernel_sum_backward(OpNode& node) {
    if (!node.input_a->trainable) return;
    float grad_val = node.output->grad[0];
    for(int i=0; i < node.input_a->total_size; i++) node.input_a->grad[i] += grad_val;
}




void exec_layer_linear(const std::string& name, const std::string& input_n, const std::string& output_n, int in_f, int out_f) {
    std::string w_name = name + "_w";
    std::string b_name = name + "_b";


    if (g_Tensors.find(w_name) == g_Tensors.end()) {
        auto w = std::make_shared<CatroidTensor>(std::vector<int>{in_f, out_f}, 0.0f, true, w_name);
        init_xavier(w->data, in_f, out_f);
        g_Tensors[w_name] = w;

        auto b = std::make_shared<CatroidTensor>(std::vector<int>{1, out_f}, 0.0f, true, b_name);

        g_Tensors[b_name] = b;
    }

    auto X = g_Tensors[input_n];
    auto W = g_Tensors[w_name];
    auto B = g_Tensors[b_name];


    std::string temp_name = name + "_tmp_matmul";

    int batch_size = X->shape[0];
    auto Temp = std::make_shared<CatroidTensor>(std::vector<int>{batch_size, out_f}, 0.0f, true, temp_name);


    kernel_matmul_forward(X, W, Temp);
    if (g_IsTraining) g_Tape.push_back({"matmul", X, W, Temp});


    auto Res = std::make_shared<CatroidTensor>(std::vector<int>{batch_size, out_f}, 0.0f, true, output_n);
    kernel_add_forward(Temp, B, Res);
    if (g_IsTraining) g_Tape.push_back({"add", Temp, B, Res});

    g_Tensors[output_n] = Res;
}


std::shared_ptr<CatroidTensor> makeInternalScalar(float val) {
    return std::make_shared<CatroidTensor>(std::vector<int>{1}, val, false);
}

extern char **environ;

Ort::Env env;
Ort::Env ort_env;
Ort::Session session{nullptr};
Ort::AllocatorWithDefaultOptions allocator;

#define LOG_TAG "PythonBridge"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

static std::string g_crashLogPath;
static JavaVM* g_JavaVM = nullptr;

static jclass g_VmManagerClass = nullptr;
static jmethodID g_OnVmOutputMethodID = nullptr;

std::map<std::string, pid_t> g_RunningVMs;
std::mutex g_VmMutex;

std::map<std::string, int> g_VmInputFds;
std::mutex g_VmInputMutex;

struct BacktraceState {
    _Unwind_Ptr* frames;
    size_t frame_count;
    size_t max_frames;
};

static _Unwind_Reason_Code unwind_callback(struct _Unwind_Context* context, void* arg) {
    BacktraceState* state = static_cast<BacktraceState*>(arg);
    _Unwind_Ptr ip = _Unwind_GetIP(context);
    if (ip) {
        if (state->frame_count < state->max_frames) {
            state->frames[state->frame_count++] = ip;
        } else {
            return _URC_END_OF_STACK;
        }
    }
    return _URC_NO_REASON;
}

void capture_backtrace(std::ostream& out) {
    const size_t max_frames = 50;
    _Unwind_Ptr frames[max_frames];
    BacktraceState state = {frames, 0, max_frames};

    _Unwind_Backtrace(unwind_callback, &state);

    for (size_t i = 0; i < state.frame_count; ++i) {
        _Unwind_Ptr addr = frames[i];
        const char* symbol = "";
        Dl_info info;
        if (dladdr((void*)addr, &info) && info.dli_sname) {
            symbol = info.dli_sname;
        }
        out << "  #" << i << ": " << (void*)addr << " (" << symbol << ")\n";
    }
}

void signal_handler(int signal_num, siginfo_t *info, void *context) {
    if (!g_crashLogPath.empty()) {
        std::ofstream log_file(g_crashLogPath, std::ios::app);
        if (log_file.is_open()) {
            log_file << "\n\n===== NATIVE CRASH DETECTED =====\n";
            log_file << "Signal: " << signal_num << " (" << strsignal(signal_num) << ")\n";
            log_file << "Fault Address: " << info->si_addr << "\n";
            log_file << "Stack Trace:\n";
            capture_backtrace(log_file);
            log_file << "===================================\n";
            log_file.close();
        }
    }
    signal(signal_num, SIG_DFL);
    raise(signal_num);
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_JavaVM = vm;
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass localVmManagerClass = env->FindClass("org/catrobat/catroid/virtualmachine/VirtualMachineManager");
    if (localVmManagerClass == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "QEMU_JNI", "Failed to find VirtualMachineManager class");
        return JNI_ERR;
    }
    g_VmManagerClass = (jclass)env->NewGlobalRef(localVmManagerClass);

    g_OnVmOutputMethodID = env->GetStaticMethodID(g_VmManagerClass, "onVmOutput", "(Ljava/lang/String;Ljava/lang/String;)V");
    if (g_OnVmOutputMethodID == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "QEMU_JNI", "Failed to find onVmOutput method");
        return JNI_ERR;
    }

    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sigemptyset(&sa.sa_mask);
    sa.sa_sigaction = signal_handler;
    sa.sa_flags = SA_SIGINFO | SA_ONSTACK;

    sigaction(SIGSEGV, &sa, nullptr);
    sigaction(SIGABRT, &sa, nullptr);

    return JNI_VERSION_1_6;
}

void vm_output_thread(int read_fd, std::string vmName) {
    JNIEnv* env;
    g_JavaVM->AttachCurrentThread(&env, nullptr);

    char buffer[1024];
    ssize_t len;

    std::string line_buffer;

    while ((len = read(read_fd, buffer, sizeof(buffer))) > 0) {
        line_buffer.append(buffer, len);

        size_t newline_pos;
        while ((newline_pos = line_buffer.find('\n')) != std::string::npos) {
            std::string line_to_send = line_buffer.substr(0, newline_pos + 1);

            if (g_VmManagerClass != nullptr && g_OnVmOutputMethodID != nullptr) {
                jstring output_j = env->NewStringUTF(line_to_send.c_str());
                jstring vmName_j = env->NewStringUTF(vmName.c_str());
                env->CallStaticVoidMethod(g_VmManagerClass, g_OnVmOutputMethodID, vmName_j, output_j);
                env->DeleteLocalRef(output_j);
                env->DeleteLocalRef(vmName_j);
            }

            line_buffer.erase(0, newline_pos + 1);
        }
    }

    close(read_fd);
    g_JavaVM->DetachCurrentThread();
    __android_log_print(ANDROID_LOG_INFO, "QEMU_LOG", "VM output thread for '%s' finished.", vmName.c_str());
}

#ifdef __aarch64__
#include "include/python3.12/Python.h"

#define JNI_PYTHON_FUNCTION(name) Java_org_catrobat_catroid_python_PythonEngine_##name
static std::atomic<PyThreadState*> g_worker_thread_state(nullptr);
static std::atomic<unsigned long> g_worker_thread_id(0);

extern "C" JNIEXPORT void JNICALL
JNI_PYTHON_FUNCTION(nativeForceStopPythonScript)(JNIEnv* env, jobject /* this */) {
    if (!Py_IsInitialized()) {
        LOGD("Python is not initialized or is finalizing. Skipping force stop.");
        return;
    }

    unsigned long thread_id = g_worker_thread_id.load();
    if (thread_id != 0) {
        LOGD("Attempting to inject SystemExit exception into thread ID: %lu", thread_id);

        PyGILState_STATE gstate = PyGILState_Ensure();

        PyThreadState_SetAsyncExc(thread_id, PyExc_SystemExit);

        PyGILState_Release(gstate);

    } else {
        LOGD("No active Python script thread to stop.");
    }
}

extern "C" JNIEXPORT void JNICALL
JNI_PYTHON_FUNCTION(nativeInitPython)(
        JNIEnv* env,
        jobject /* this */,
        jobjectArray modulePaths) {

    PyConfig config;
    PyConfig_InitPythonConfig(&config);
    config.install_signal_handlers = 0;
    config.module_search_paths_set = 1;

    int numPaths = env->GetArrayLength(modulePaths);
    LOGD("Preparing Python with %d module paths...", numPaths);
    for (int i = 0; i < numPaths; i++) {
        jstring path_jstr = (jstring) env->GetObjectArrayElement(modulePaths, i);
        const char* path_cstr = env->GetStringUTFChars(path_jstr, 0);
        wchar_t* path_wstr = Py_DecodeLocale(path_cstr, NULL);

        PyWideStringList_Append(&config.module_search_paths, path_wstr);
        LOGD("Added path %d: %s", i + 1, path_cstr);

        PyMem_RawFree(path_wstr);
        env->ReleaseStringUTFChars(path_jstr, path_cstr);
        env->DeleteLocalRef(path_jstr);
    }

    PyStatus status = Py_InitializeFromConfig(&config);
    if (PyStatus_Exception(status)) {
        LOGD("FATAL: Py_InitializeFromConfig failed.");
        Py_ExitStatusException(status);
    } else {
        LOGD("!!! SUCCESS: Python has been initialized correctly !!!");
    }

    PyConfig_Clear(&config);
}

extern "C" JNIEXPORT void JNICALL
JNI_PYTHON_FUNCTION(nativeFinalizePython)(JNIEnv* env, jobject /* this */) {
    if (Py_IsInitialized()) {
        Py_FinalizeEx();
        LOGD("Python environment has been finalized.");
    } else {
        LOGD("Python was not initialized, skipping finalization.");
    }
}

extern "C" JNIEXPORT void JNICALL
JNI_PYTHON_FUNCTION(nativeInitPython2)(
        JNIEnv* env,
        jobject /* this */,
        jstring pythonHome,
        jstring projectLibsPath) {

    const char* pyHome_cstr = env->GetStringUTFChars(pythonHome, 0);
    const char* pyLibsPath_cstr = env->GetStringUTFChars(projectLibsPath, 0);
    wchar_t *pyHomeW = Py_DecodeLocale(pyHome_cstr, NULL);
    wchar_t *pyLibsPathW = Py_DecodeLocale(pyLibsPath_cstr, NULL);
    PyConfig config;
    PyConfig_InitPythonConfig(&config);
    bool success = false;

    if (pyHomeW && pyLibsPathW) {
        LOGD("Preparing Python with FULL PATH OVERRIDE...");
        config.install_signal_handlers = 0;
        config.verbose = 0;
        PyStatus status;

        config.module_search_paths_set = 1;

        std::wstring path_stdlib(pyHomeW);
        std::wstring path_pylibs(pyLibsPathW);
        std::wstring path_dynload = std::wstring(pyHomeW) + L"/lib-dynload";

        status = PyWideStringList_Append(&config.module_search_paths, path_stdlib.c_str());
        if (!PyStatus_Exception(status)) {
            status = PyWideStringList_Append(&config.module_search_paths, path_pylibs.c_str());
        }
        if (!PyStatus_Exception(status)) {
            status = PyWideStringList_Append(&config.module_search_paths, path_dynload.c_str());
        }

        if (PyStatus_Exception(status)) {
            LOGD("FATAL: Failed to construct module search paths.");
        } else {
            LOGD("Module Search Paths set to:");
            LOGD("1: %ls", path_stdlib.c_str());
            LOGD("2: %ls", path_pylibs.c_str());
            LOGD("3: %ls", path_dynload.c_str());
            LOGD("Initializing with Py_InitializeFromConfig...");

            status = Py_InitializeFromConfig(&config);
            if (PyStatus_Exception(status)) {
                LOGD("FATAL: Py_InitializeFromConfig failed.");
                Py_ExitStatusException(status);
            } else {
                LOGD("!!! SUCCESS: Python has been initialized correctly !!!");
                success = true;
            }
        }
    } else {
        LOGD("FATAL: Failed to decode Python paths.");
    }

    PyConfig_Clear(&config);
    if (pyHomeW) PyMem_RawFree(pyHomeW);
    if (pyLibsPathW) PyMem_RawFree(pyLibsPathW);
    env->ReleaseStringUTFChars(pythonHome, pyHome_cstr);
    env->ReleaseStringUTFChars(projectLibsPath, pyLibsPath_cstr);

    if (!success) {
        LOGD("Python initialization failed.");
    }
}

extern "C" JNIEXPORT jstring JNICALL
JNI_PYTHON_FUNCTION(nativeRunScript2)(
        JNIEnv* env,
        jobject /* this */,
        jstring script) {

    if (!Py_IsInitialized()) {
        return env->NewStringUTF("Python is not initialized.");
    }

    PyGILState_STATE gstate = PyGILState_Ensure();

    const char* scriptStr = env->GetStringUTFChars(script, 0);
    int result = PyRun_SimpleString(scriptStr);
    env->ReleaseStringUTFChars(script, scriptStr);

    jstring errorMessage = NULL;

    if (result != 0) {
        LOGD("Python script failed! Using traceback module to get full error...");

        if (PyErr_Occurred()) {
            PyObject *pType, *pValue, *pTraceback;
            PyErr_Fetch(&pType, &pValue, &pTraceback);
            PyErr_NormalizeException(&pType, &pValue, &pTraceback);

            PyErr_Restore(pType, pValue, pTraceback);

            PyObject* traceback_module = PyImport_ImportModule("traceback");
            if (traceback_module != NULL) {
                PyObject* format_exc_func = PyObject_GetAttrString(traceback_module, "format_exc");
                if (format_exc_func != NULL) {
                    PyObject* formatted_exception = PyObject_CallObject(format_exc_func, NULL);
                    if (formatted_exception != NULL) {
                        const char* err_str = PyUnicode_AsUTF8(formatted_exception);
                        if (err_str) {
                            errorMessage = env->NewStringUTF(err_str);
                        }
                        Py_DECREF(formatted_exception);
                    }
                    Py_DECREF(format_exc_func);
                }
                Py_DECREF(traceback_module);
            }
        }
    }

    if (errorMessage == NULL && result != 0) {
        errorMessage = env->NewStringUTF("Unknown Python error, and traceback module failed.");
    }

    PyGILState_Release(gstate);
    return errorMessage;
}

extern "C" JNIEXPORT jstring JNICALL
JNI_PYTHON_FUNCTION(nativeRunScript)(
        JNIEnv* env,
        jobject /* this */,
        jstring script) {

    if (!Py_IsInitialized()) {
        return env->NewStringUTF("Python is not initialized.");
    }

    PyGILState_STATE gstate = PyGILState_Ensure();

    g_worker_thread_id = PyThread_get_thread_ident();

    PyObject* main_module = PyImport_AddModule("__main__");
    PyObject* main_dict = PyModule_GetDict(main_module);

    const char* capture_script =
            "import sys, io, traceback\n"
            "def __run_and_capture(code_to_run):\n"
            "    buffer = io.StringIO()\n"
            "    sys.stdout = buffer\n"
            "    sys.stderr = buffer\n"
            "    try:\n"
            "        exec(code_to_run, globals())\n"
            "    except SystemExit:\n"
            "        print('\\nScipt stopped')\n"
            "    except Exception:\n"
            "        traceback.print_exc()\n"
            "    sys.stdout = sys.__stdout__\n"
            "    sys.stderr = sys.__stderr__\n"
            "    return buffer.getvalue()\n";

    PyRun_String(capture_script, Py_file_input, main_dict, main_dict);

    PyObject* capture_func = PyDict_GetItemString(main_dict, "__run_and_capture");

    jstring result_string = NULL;

    if (capture_func && PyCallable_Check(capture_func)) {
        PyObject* pArgs = PyTuple_New(1);
        const char* script_cstr = env->GetStringUTFChars(script, 0);
        PyObject* pScript = PyUnicode_FromString(script_cstr);
        PyTuple_SetItem(pArgs, 0, pScript);
        env->ReleaseStringUTFChars(script, script_cstr);

        PyObject* pResult = PyObject_CallObject(capture_func, pArgs);
        Py_DECREF(pArgs);

        if (pResult != NULL) {
            const char* result_cstr = PyUnicode_AsUTF8(pResult);
            if (result_cstr) {
                result_string = env->NewStringUTF(result_cstr);
            }
            Py_DECREF(pResult);
        } else {
            PyErr_Print();
            result_string = env->NewStringUTF("FATAL: The C++ capture function itself failed.");
        }
    } else {
        result_string = env->NewStringUTF("FATAL: Could not find the __run_and_capture helper function.");
    }
     g_worker_thread_id = 0;

    PyGILState_Release(gstate);
    return result_string;
}

#else

#include <android/log.h>
#define JNI_PYTHON_FUNCTION(name) Java_org_catrobat_catroid_python_PythonEngine_##name

static std::atomic<unsigned long> g_worker_thread_id(0);

extern "C" JNIEXPORT void JNICALL
JNI_PYTHON_FUNCTION(nativeForceStopPythonScript)(JNIEnv* env, jobject /* this */) {
    LOGD("Python is not supported on this device architecture.");
}

extern "C" JNIEXPORT void JNICALL
JNI_PYTHON_FUNCTION(nativeInitPython)(JNIEnv*, jobject, jobjectArray) {
    __android_log_print(ANDROID_LOG_WARN, "PythonEngine", "nativeInitPython called on unsupported architecture. Doing nothing.");
}

extern "C" JNIEXPORT jstring JNICALL
JNI_PYTHON_FUNCTION(nativeRunScript)(JNIEnv* env, jobject, jstring) {
    return env->NewStringUTF("Python is not supported on this device architecture.");
}

extern "C" JNIEXPORT void JNICALL
JNI_PYTHON_FUNCTION(nativeFinalizePython)(JNIEnv* env, jobject /* this */) {
    LOGD("Python is not supported on this device architecture.");
}

#endif

#define JNI_FUNCTION(name) Java_org_catrobat_catroid_NN_OnnxSessionManager_##name

extern "C" JNIEXPORT jint JNICALL
JNI_FUNCTION(loadModelJNI)(JNIEnv* env, jobject /* this */, jstring modelPath) {
    const char* model_path_chars = env->GetStringUTFChars(modelPath, nullptr);
    try {
        session = Ort::Session(::env, model_path_chars, Ort::SessionOptions{nullptr});
    } catch (const Ort::Exception& e) {
        env->ReleaseStringUTFChars(modelPath, model_path_chars);
        return -1;
    }
    env->ReleaseStringUTFChars(modelPath, model_path_chars);
    return 0;
}

extern "C" JNIEXPORT jfloatArray JNICALL
JNI_FUNCTION(runInferenceJNI)(JNIEnv* env, jobject /* this */, jfloatArray inputData) {
    if (!session) { return nullptr; }

    if (session.GetInputCount() == 0) { return nullptr; }
    Ort::AllocatedStringPtr input_name_ptr = session.GetInputNameAllocated(0, allocator);
    const char* input_name_chars[] = { input_name_ptr.get() };


    Ort::TypeInfo input_type_info = session.GetInputTypeInfo(0);
    auto tensor_info = input_type_info.GetTensorTypeAndShapeInfo();
    std::vector<int64_t> input_shape = tensor_info.GetShape();

    for (int64_t &dim : input_shape) {
        if (dim < 1) {
            dim = 1;
        }
    }

    jsize userInputSize = env->GetArrayLength(inputData);
    size_t expectedInputSize = std::accumulate(input_shape.begin(), input_shape.end(), 1LL, std::multiplies<int64_t>());

    if (userInputSize != expectedInputSize) {
        return nullptr;
    }

    jfloat* input_floats = env->GetFloatArrayElements(inputData, nullptr);
    std::vector<float> input_vec(input_floats, input_floats + userInputSize);

    auto memory_info = Ort::MemoryInfo::CreateCpu(OrtArenaAllocator, OrtMemTypeDefault);
    Ort::Value input_tensor = Ort::Value::CreateTensor<float>(
            memory_info, input_vec.data(), input_vec.size(), input_shape.data(), input_shape.size());

    if (session.GetOutputCount() == 0) { return nullptr; }
    Ort::AllocatedStringPtr output_name_ptr = session.GetOutputNameAllocated(0, allocator);
    const char* output_name_chars[] = { output_name_ptr.get() };


    auto output_tensors = session.Run(Ort::RunOptions{nullptr}, input_name_chars, &input_tensor, 1, output_name_chars, 1);

    const auto& output_tensor = output_tensors[0];
    auto output_shape_info = output_tensor.GetTensorTypeAndShapeInfo();
    size_t output_size = output_shape_info.GetElementCount();
    const float* output_data = output_tensor.GetTensorData<float>();

    jfloatArray resultArray = env->NewFloatArray(output_size);
    env->SetFloatArrayRegion(resultArray, 0, output_size, output_data);

    env->ReleaseFloatArrayElements(inputData, input_floats, 0);
    return resultArray;
}

extern "C" JNIEXPORT void JNICALL
JNI_FUNCTION(unloadModelJNI)(JNIEnv* env, jobject /* this */) {
    session = Ort::Session(nullptr);
}

struct Transform {
    float x, y;
    float scaleX, scaleY;
    float rotation;
    float originX, originY;
};

std::vector<float> transform_vertices(const std::vector<float>& vertices, const Transform& transform) {
    std::vector<float> transformed_vertices;
    transformed_vertices.reserve(vertices.size());

    float rotation_rad = transform.rotation * (M_PI / 180.0f);
    float cos_r = cos(rotation_rad);
    float sin_r = sin(rotation_rad);

    for (size_t i = 0; i < vertices.size(); i += 2) {
        float vx = vertices[i] - transform.originX;
        float vy = vertices[i+1] - transform.originY;

        vx *= transform.scaleX;
        vy *= transform.scaleY;

        float rotated_x = vx * cos_r - vy * sin_r;
        float rotated_y = vx * sin_r + vy * cos_r;

        transformed_vertices.push_back(rotated_x + transform.originX + transform.x);
        transformed_vertices.push_back(rotated_y + transform.originY + transform.y);
    }

    return transformed_vertices;
}

#define JNI_OPTIMIZER_FUNCTION(name) Java_org_catrobat_catroid_utils_NativeLookOptimizer_##name

extern "C" JNIEXPORT jfloatArray JNICALL
JNI_OPTIMIZER_FUNCTION(transformPolygon)(
        JNIEnv* env,
        jclass /* this */,
        jfloatArray jvertices,
        jfloat x, jfloat y,
        jfloat scaleX, jfloat scaleY,
        jfloat rotation,
        jfloat originX, jfloat originY) {

    jsize len = env->GetArrayLength(jvertices);
    jfloat* vertex_elements = env->GetFloatArrayElements(jvertices, nullptr);
    std::vector<float> vertices_vec(vertex_elements, vertex_elements + len);
    env->ReleaseFloatArrayElements(jvertices, vertex_elements, JNI_ABORT);

    Transform t = {x, y, scaleX, scaleY, rotation, originX, originY};

    std::vector<float> transformed_vertices = transform_vertices(vertices_vec, t);

    jfloatArray result_array = env->NewFloatArray(transformed_vertices.size());
    env->SetFloatArrayRegion(result_array, 0, transformed_vertices.size(), transformed_vertices.data());

    return result_array;
}
std::vector<float> calculate_aabb(const Transform& transform, float width, float height) {
    float local_coords[8] = {
            0, 0,
            width, 0,
            width, height,
            0, height
    };

    float rotation_rad = transform.rotation * (M_PI / 180.0f);
    float cos_r = cos(rotation_rad);
    float sin_r = sin(rotation_rad);

    std::vector<float> transformed_x;
    std::vector<float> transformed_y;
    transformed_x.reserve(4);
    transformed_y.reserve(4);

    for (size_t i = 0; i < 8; i += 2) {
        float vx = local_coords[i] - transform.originX;
        float vy = local_coords[i+1] - transform.originY;

        vx *= transform.scaleX;
        vy *= transform.scaleY;

        float rotated_x = vx * cos_r - vy * sin_r;
        float rotated_y = vx * sin_r + vy * cos_r;

        transformed_x.push_back(rotated_x + transform.originX + transform.x);
        transformed_y.push_back(rotated_y + transform.originY + transform.y);
    }

    float min_x = *std::min_element(transformed_x.begin(), transformed_x.end());
    float max_x = *std::max_element(transformed_x.begin(), transformed_x.end());
    float min_y = *std::min_element(transformed_y.begin(), transformed_y.end());
    float max_y = *std::max_element(transformed_y.begin(), transformed_y.end());

    return {min_x, min_y, max_x - min_x, max_y - min_y};
}

extern "C" JNIEXPORT jfloatArray JNICALL
JNI_OPTIMIZER_FUNCTION(getTransformedBoundingBox)(
        JNIEnv* env,
        jclass /* this */,
        jfloat x, jfloat y,
        jfloat width, jfloat height,
        jfloat scaleX, jfloat scaleY,
        jfloat rotation,
        jfloat originX, jfloat originY) {

    Transform t = {x, y, scaleX, scaleY, rotation, originX, originY};
    std::vector<float> aabb = calculate_aabb(t, width / scaleX, height / scaleY);

    jfloatArray result_array = env->NewFloatArray(4);
    env->SetFloatArrayRegion(result_array, 0, 4, aabb.data());
    return result_array;
}

struct AABB {
    double minX, minY, maxX, maxY;
};

bool aabbs_overlap(const AABB& a, const AABB& b) {
    if (a.maxX < b.minX || b.maxX < a.minX) return false;
    if (a.maxY < b.minY || b.maxY < a.minY) return false;
    return true;
}

std::vector<uint32_t> triangulate(const std::vector<double>& vertices) {
    if (vertices.size() < 6) {
        return {};
    }
    std::vector<std::vector<std::array<double, 2>>> polygon_data;
    std::vector<std::array<double, 2>> ring;
    ring.reserve(vertices.size() / 2);
    for (size_t i = 0; i < vertices.size(); i += 2) {
        ring.push_back({vertices[i], vertices[i+1]});
    }
    polygon_data.push_back(ring);

    return mapbox::earcut<uint32_t>(polygon_data);
}

bool polygons_overlap(const double* vertsA, int countA, const double* vertsB, int countB) {
    for (int i = 0; i < countA; i += 2) {
        double p1x = vertsA[i];
        double p1y = vertsA[i + 1];
        double p2x = vertsA[(i + 2) % countA];
        double p2y = vertsA[(i + 3) % countA];
        double axisX = -(p2y - p1y);
        double axisY = p2x - p1x;

        double len = std::sqrt(axisX * axisX + axisY * axisY);
        if (len > 1e-8) {
            axisX /= len;
            axisY /= len;
        }

        double minA = 1e18, maxA = -1e18, minB = 1e18, maxB = -1e18;
        for (int j = 0; j < countA; j += 2) {
            double dot = vertsA[j] * axisX + vertsA[j + 1] * axisY;
            minA = std::min(minA, dot);
            maxA = std::max(maxA, dot);
        }
        for (int j = 0; j < countB; j += 2) {
            double dot = vertsB[j] * axisX + vertsB[j + 1] * axisY;
            minB = std::min(minB, dot);
            maxB = std::max(maxB, dot);
        }
        if (maxA < minB || maxB < minA) return false;
    }

    for (int i = 0; i < countB; i += 2) {
        double p1x = vertsB[i];
        double p1y = vertsB[i + 1];
        double p2x = vertsB[(i + 2) % countB];
        double p2y = vertsB[(i + 3) % countB];
        double axisX = -(p2y - p1y);
        double axisY = p2x - p1x;

        double len = std::sqrt(axisX * axisX + axisY * axisY);
        if (len > 1e-8) {
            axisX /= len;
            axisY /= len;
        }

        double minA = 1e18, maxA = -1e18, minB = 1e18, maxB = -1e18;
        for (int j = 0; j < countA; j += 2) {
            double dot = vertsA[j] * axisX + vertsA[j+1] * axisY;
            minA = std::min(minA, dot);
            maxA = std::max(maxA, dot);
        }
        for (int j = 0; j < countB; j += 2) {
            double dot = vertsB[j] * axisX + vertsB[j+1] * axisY;
            minB = std::min(minB, dot);
            maxB = std::max(maxB, dot);
        }
        if (maxA < minB || maxB < minA) return false;
    }
    return true;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_org_catrobat_catroid_utils_NativeLookOptimizer_checkSingleCollision(
        JNIEnv* env,
        jclass /* this */,
        jobjectArray firstLookPolygons,
        jobjectArray secondLookPolygons
) {
    int firstPolygonCount = env->GetArrayLength(firstLookPolygons);
    std::vector<std::vector<double>> firstPolys(firstPolygonCount);
    for (int i = 0; i < firstPolygonCount; ++i) {
        auto poly_jfloatArray = (jfloatArray)env->GetObjectArrayElement(firstLookPolygons, i);
        jfloat* verts_float = env->GetFloatArrayElements(poly_jfloatArray, nullptr);
        int count = env->GetArrayLength(poly_jfloatArray);
        firstPolys[i].assign(verts_float, verts_float + count);
        env->ReleaseFloatArrayElements(poly_jfloatArray, verts_float, JNI_ABORT);
        env->DeleteLocalRef(poly_jfloatArray);
    }

    int secondPolygonCount = env->GetArrayLength(secondLookPolygons);
    std::vector<std::vector<double>> secondPolys(secondPolygonCount);
    for (int i = 0; i < secondPolygonCount; ++i) {
        auto poly_jfloatArray = (jfloatArray)env->GetObjectArrayElement(secondLookPolygons, i);
        jfloat* verts_float = env->GetFloatArrayElements(poly_jfloatArray, nullptr);
        int count = env->GetArrayLength(poly_jfloatArray);
        secondPolys[i].assign(verts_float, verts_float + count);
        env->ReleaseFloatArrayElements(poly_jfloatArray, verts_float, JNI_ABORT);
        env->DeleteLocalRef(poly_jfloatArray);
    }

    for (const auto& polyA_verts : firstPolys) {
        std::vector<uint32_t> trianglesA_indices = triangulate(polyA_verts);

        for (const auto& polyB_verts : secondPolys) {
            std::vector<uint32_t> trianglesB_indices = triangulate(polyB_verts);

            for (size_t i = 0; i < trianglesA_indices.size(); i += 3) {
                double triangleA[6] = {
                        polyA_verts[trianglesA_indices[i] * 2], polyA_verts[trianglesA_indices[i] * 2 + 1],
                        polyA_verts[trianglesA_indices[i+1] * 2], polyA_verts[trianglesA_indices[i+1] * 2 + 1],
                        polyA_verts[trianglesA_indices[i+2] * 2], polyA_verts[trianglesA_indices[i+2] * 2 + 1]
                };

                for (size_t j = 0; j < trianglesB_indices.size(); j += 3) {
                    double triangleB[6] = {
                            polyB_verts[trianglesB_indices[j] * 2], polyB_verts[trianglesB_indices[j] * 2 + 1],
                            polyB_verts[trianglesB_indices[j+1] * 2], polyB_verts[trianglesB_indices[j+1] * 2 + 1],
                            polyB_verts[trianglesB_indices[j+2] * 2], polyB_verts[trianglesB_indices[j+2] * 2 + 1]
                    };

                    if (polygons_overlap(triangleA, 6, triangleB, 6)) {
                        return JNI_TRUE;
                    }
                }
            }
        }
    }

    return JNI_FALSE;
}


extern "C" JNIEXPORT jintArray JNICALL
Java_org_catrobat_catroid_utils_NativeLookOptimizer_checkAllCollisions(
        JNIEnv* env,
        jclass /* this */,
        jobjectArray allSpritesPolygons
) {
    int spriteCount = env->GetArrayLength(allSpritesPolygons);
    if (spriteCount < 2) {
        return env->NewIntArray(0);
    }

    std::vector<std::vector<std::vector<double>>> spritesData(spriteCount);
    std::vector<AABB> spriteAABBs(spriteCount);

    for (int i = 0; i < spriteCount; ++i) {
        auto polygonsArray = (jobjectArray)env->GetObjectArrayElement(allSpritesPolygons, i);
        int polygonCount = env->GetArrayLength(polygonsArray);
        spritesData[i].resize(polygonCount);

        double spriteMinX = 1e18, spriteMinY = 1e18, spriteMaxX = -1e18, spriteMaxY = -1e18;

        for (int j = 0; j < polygonCount; ++j) {
            auto verticesArray = (jfloatArray)env->GetObjectArrayElement(polygonsArray, j);
            jfloat* vertices_float = env->GetFloatArrayElements(verticesArray, nullptr);
            int vertexCount = env->GetArrayLength(verticesArray);

            spritesData[i][j].assign(vertices_float, vertices_float + vertexCount);

            for (int k = 0; k < vertexCount; k += 2) {
                spriteMinX = std::min(spriteMinX, (double)vertices_float[k]);
                spriteMinY = std::min(spriteMinY, (double)vertices_float[k+1]);
                spriteMaxX = std::max(spriteMaxX, (double)vertices_float[k]);
                spriteMaxY = std::max(spriteMaxY, (double)vertices_float[k+1]);
            }
            env->ReleaseFloatArrayElements(verticesArray, vertices_float, JNI_ABORT);
            env->DeleteLocalRef(verticesArray);
        }
        spriteAABBs[i] = {spriteMinX, spriteMinY, spriteMaxX, spriteMaxY};
        env->DeleteLocalRef(polygonsArray);
    }

    std::vector<int> collidingPairs;
    for (int i = 0; i < spriteCount; ++i) {
        for (int j = i + 1; j < spriteCount; ++j) {
            if (aabbs_overlap(spriteAABBs[i], spriteAABBs[j])) {
                bool collisionFound = false;
                for (const auto& polyA : spritesData[i]) {
                    std::vector<uint32_t> trianglesA = triangulate(polyA);

                    for (const auto& polyB : spritesData[j]) {
                        std::vector<uint32_t> trianglesB = triangulate(polyB);

                        for (size_t ti_a = 0; ti_a < trianglesA.size(); ti_a += 3) {
                            double triangleA_verts[6] = {
                                    polyA[trianglesA[ti_a] * 2], polyA[trianglesA[ti_a] * 2 + 1],
                                    polyA[trianglesA[ti_a+1] * 2], polyA[trianglesA[ti_a+1] * 2 + 1],
                                    polyA[trianglesA[ti_a+2] * 2], polyA[trianglesA[ti_a+2] * 2 + 1]
                            };

                            for (size_t ti_b = 0; ti_b < trianglesB.size(); ti_b += 3) {
                                double triangleB_verts[6] = {
                                        polyB[trianglesB[ti_b] * 2], polyB[trianglesB[ti_b] * 2 + 1],
                                        polyB[trianglesB[ti_b+1] * 2], polyB[trianglesB[ti_b+1] * 2 + 1],
                                        polyB[trianglesB[ti_b+2] * 2], polyB[trianglesB[ti_b+2] * 2 + 1]
                                };

                                if (polygons_overlap(triangleA_verts, 6, triangleB_verts, 6)) {
                                    collidingPairs.push_back(i);
                                    collidingPairs.push_back(j);
                                    collisionFound = true;
                                    break;
                                }
                            }
                            if (collisionFound) break;
                        }
                    }
                    if (collisionFound) break;
                }
            }
        }
    }

    jintArray resultArray = env->NewIntArray(collidingPairs.size());
    if (!collidingPairs.empty()) {
        env->SetIntArrayRegion(resultArray, 0, collidingPairs.size(), collidingPairs.data());
    }
    return resultArray;
}

struct CoreAPI {
    void (*initialize)(ResolvePathCallback);
    void (*on_surface_created)(const char*, ANativeWindow*);
    void (*on_surface_changed)(const char*, int, int);
    void (*on_surface_destroyed)(const char*);
    void (*on_touch_event)(const char*, int, float, float, int);
    void (*shutdown)();
};

struct GLInstance {
    void*               so_handle;
    CoreAPI             api;
    ANativeWindow*      window;
    std::string         view_name;
};

std::map<std::string, GLInstance> g_GlInstances;
std::mutex g_Mutex;

#define JNI_GL_FUNCTION(name) Java_org_catrobat_catroid_utils_NativeBridge_##name

extern "C" {

const char* resolve_project_file_path(const char* fileName) {
    static std::string result_path;

    JNIEnv* env = nullptr;
    if (g_JavaVM->AttachCurrentThreadAsDaemon(&env, nullptr) != JNI_OK) {
        return nullptr;
    }
    if (!env) return nullptr;

    jclass bridgeClass = env->FindClass("org/catrobat/catroid/utils/NativeBridge");
    if (!bridgeClass) return nullptr;

    jmethodID methodId = env->GetStaticMethodID(bridgeClass, "getProjectFilePath", "(Ljava/lang/String;)Ljava/lang/String;");
    if (!methodId) return nullptr;

    jstring jFileName = env->NewStringUTF(fileName);
    auto jPath = (jstring)env->CallStaticObjectMethod(bridgeClass, methodId, jFileName);

    result_path.clear();
    if (jPath) {
        const char* path_chars = env->GetStringUTFChars(jPath, nullptr);
        result_path = path_chars;
        env->ReleaseStringUTFChars(jPath, path_chars);
        env->DeleteLocalRef(jPath);
    }

    env->DeleteLocalRef(jFileName);

    return result_path.empty() ? nullptr : result_path.c_str();
}

JNIEXPORT void JNICALL
JNI_GL_FUNCTION(attachSoToView)(JNIEnv *env, jobject thiz, jstring view_name_j, jstring path_to_so_j) {
    const char* viewName = env->GetStringUTFChars(view_name_j, nullptr);
    const char* pathToSo = env->GetStringUTFChars(path_to_so_j, nullptr);

    std::lock_guard<std::mutex> lock(g_Mutex);

    void* handle = dlopen(pathToSo, RTLD_LAZY);
    if (!handle) {
        __android_log_print(ANDROID_LOG_ERROR, "NativeBridge", "Failed to load .so from '%s': %s", pathToSo, dlerror());
        env->ReleaseStringUTFChars(view_name_j, viewName);
        env->ReleaseStringUTFChars(path_to_so_j, pathToSo);
        return;
    }

    GLInstance instance;
    instance.so_handle = handle;
    instance.window = nullptr;
    instance.view_name = viewName;

    instance.api.initialize = (void (*)(ResolvePathCallback))dlsym(handle, "core_initialize");
    instance.api.on_surface_created = (void (*)(const char*, ANativeWindow*))dlsym(handle, "core_on_surface_created");
    instance.api.on_surface_changed = (void (*)(const char*, int, int))dlsym(handle, "core_on_surface_changed");
    instance.api.on_surface_destroyed = (void (*)(const char*))dlsym(handle, "core_on_surface_destroyed");
    instance.api.shutdown = (void (*)())dlsym(handle, "core_shutdown");
    instance.api.on_touch_event = (void (*)(const char*, int, float, float, int))dlsym(handle, "core_on_touch_event");

    if (instance.api.initialize) {
        try {
            instance.api.initialize(resolve_project_file_path);
        } catch (const std::exception& e) {
            std::ofstream log_file(g_crashLogPath, std::ios::app);
            log_file << "C++ Exception in initialize: " << e.what() << "\n";
        } catch (...) {
            std::ofstream log_file(g_crashLogPath, std::ios::app);
            log_file << "Unknown C++ Exception in initialize\n";
        }
    }

    g_GlInstances[viewName] = instance;
    __android_log_print(ANDROID_LOG_INFO, "NativeBridge", "Attached .so to view '%s'", viewName);

    env->ReleaseStringUTFChars(view_name_j, viewName);
    env->ReleaseStringUTFChars(path_to_so_j, pathToSo);
}

JNIEXPORT void JNICALL
JNI_GL_FUNCTION(onSurfaceCreated)(JNIEnv *env, jobject thiz, jstring view_name_j, jobject surface) {
    const char* viewName = env->GetStringUTFChars(view_name_j, nullptr);
    std::lock_guard<std::mutex> lock(g_Mutex);

    if (g_GlInstances.count(viewName)) {
        GLInstance& instance = g_GlInstances[viewName];
        instance.window = ANativeWindow_fromSurface(env, surface);
        if (instance.api.on_surface_created) {
            try {
                instance.api.on_surface_created(viewName, instance.window);
            } catch (const std::exception& e) {
                std::ofstream log_file(g_crashLogPath, std::ios::app);
                log_file << "C++ Exception in on_surface_created: " << e.what() << "\n";
            } catch (...) {
                std::ofstream log_file(g_crashLogPath, std::ios::app);
                log_file << "Unknown C++ Exception in on_surface_created\n";
            }
        }
    }
    env->ReleaseStringUTFChars(view_name_j, viewName);
}

JNIEXPORT void JNICALL
JNI_GL_FUNCTION(onSurfaceChanged)(JNIEnv *env, jobject thiz, jstring view_name_j, jint width, jint height) {
    const char* viewName = env->GetStringUTFChars(view_name_j, nullptr);
    std::lock_guard<std::mutex> lock(g_Mutex);
    if (g_GlInstances.count(viewName)) {
        GLInstance& instance = g_GlInstances[viewName];
        if (instance.api.on_surface_changed) {
            try {
                instance.api.on_surface_changed(viewName, width, height);
            } catch (const std::exception& e) {
                std::ofstream log_file(g_crashLogPath, std::ios::app);
                log_file << "C++ Exception in on_surface_changed: " << e.what() << "\n";
            } catch (...) {
                std::ofstream log_file(g_crashLogPath, std::ios::app);
                log_file << "Unknown C++ Exception in on_surface_changed\n";
            }
        }
    }
    env->ReleaseStringUTFChars(view_name_j, viewName);
}

JNIEXPORT void JNICALL
JNI_GL_FUNCTION(onSurfaceDestroyed)(JNIEnv *env, jobject thiz, jstring view_name_j) {
    const char* viewName = env->GetStringUTFChars(view_name_j, nullptr);
    std::lock_guard<std::mutex> lock(g_Mutex);
    if (g_GlInstances.count(viewName)) {
        GLInstance& instance = g_GlInstances[viewName];
        if (instance.api.on_surface_destroyed) {
            try {
                instance.api.on_surface_destroyed(viewName);
            } catch (const std::exception& e) {
                std::ofstream log_file(g_crashLogPath, std::ios::app);
                log_file << "C++ Exception in on_surface_destroyed: " << e.what() << "\n";
            } catch (...) {
                std::ofstream log_file(g_crashLogPath, std::ios::app);
                log_file << "Unknown C++ Exception in on_surface_destroyed\n";
            }
        }
        if (instance.window) {
            ANativeWindow_release(instance.window);
            instance.window = nullptr;
        }
    }
    env->ReleaseStringUTFChars(view_name_j, viewName);
}

JNIEXPORT void JNICALL
JNI_GL_FUNCTION(cleanupInstance)(JNIEnv *env, jobject thiz, jstring view_name_j) {
    const char* viewName = env->GetStringUTFChars(view_name_j, nullptr);
    std::lock_guard<std::mutex> lock(g_Mutex);

    auto it = g_GlInstances.find(viewName);
    if (it != g_GlInstances.end()) {
        GLInstance& instance = it->second;

        if (instance.window) {
            ANativeWindow_release(instance.window);
            instance.window = nullptr;
        }

        if (instance.api.shutdown) {
            try {
                instance.api.shutdown();
            } catch (const std::exception& e) {
                std::ofstream log_file(g_crashLogPath, std::ios::app);
                log_file << "C++ Exception in shutdown: " << e.what() << "\n";
            } catch (...) {
                std::ofstream log_file(g_crashLogPath, std::ios::app);
                log_file << "Unknown C++ Exception in shutdown\n";
            }
        }
        if (instance.so_handle) {
            dlclose(instance.so_handle);
        }
        g_GlInstances.erase(it);
        __android_log_print(ANDROID_LOG_INFO, "NativeBridge", "Cleaned up instance '%s'", viewName);
    }
    env->ReleaseStringUTFChars(view_name_j, viewName);
}

JNIEXPORT void JNICALL
JNI_GL_FUNCTION(cleanupAllInstances)(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(g_Mutex);
    for (auto const& [name, instance] : g_GlInstances) {
        if (instance.window) {
            ANativeWindow_release(instance.window);
        }

        if (instance.api.shutdown) {
            try {
                instance.api.shutdown();
            } catch (const std::exception& e) {
                std::ofstream log_file(g_crashLogPath, std::ios::app);
                log_file << "C++ Exception in shutdown: " << e.what() << "\n";
            } catch (...) {
                std::ofstream log_file(g_crashLogPath, std::ios::app);
                log_file << "Unknown C++ Exception in shutdown\n";
            }
        }
        if (instance.so_handle) {
            dlclose(instance.so_handle);
        }
    }
    g_GlInstances.clear();
    __android_log_print(ANDROID_LOG_INFO, "NativeBridge", "Cleaned up ALL instances.");
}

JNIEXPORT void JNICALL
JNI_GL_FUNCTION(onTouchEvent)(JNIEnv *env, jobject thiz, jstring view_name_j, jint action, jfloat x, jfloat y, jint pointerId) {
    const char* viewName = env->GetStringUTFChars(view_name_j, nullptr);
    std::lock_guard<std::mutex> lock(g_Mutex);
    if (g_GlInstances.count(viewName)) {
        GLInstance& instance = g_GlInstances[viewName];
        if (instance.api.on_touch_event) {
            try {
                instance.api.on_touch_event(viewName, action, x, y, pointerId);
            } catch (const std::exception& e) {
                std::ofstream log_file(g_crashLogPath, std::ios::app);
                log_file << "C++ Exception in on_touch_event: " << e.what() << "\n";
            } catch (...) {
                std::ofstream log_file(g_crashLogPath, std::ios::app);
                log_file << "Unknown C++ Exception in on_touch_event\n";
            }
        }
    }
    env->ReleaseStringUTFChars(view_name_j, viewName);
}

JNIEXPORT void JNICALL
JNI_GL_FUNCTION(setCrashLogPath)(JNIEnv* env, jobject thiz, jstring path_j) {
    const char* path_c = env->GetStringUTFChars(path_j, nullptr);
    if (path_c) {
        g_crashLogPath = path_c;
        __android_log_print(ANDROID_LOG_INFO, "NativeBridge", "Crash log path set to: %s", g_crashLogPath.c_str());
    }
    env->ReleaseStringUTFChars(path_j, path_c);
}
} // extern "C"

#define JNI_VM_FUNCTION(name) Java_org_catrobat_catroid_virtualmachine_VirtualMachineManager_##name

extern "C" {
void log_pipe_thread(int read_fd) {
    char buffer[256];
    ssize_t len;
    while ((len = read(read_fd, buffer, sizeof(buffer) - 1)) > 0) {
        buffer[len] = '\0';
        __android_log_print(ANDROID_LOG_INFO, "QEMU_LOG", "%s", buffer);
    }
    close(read_fd);
    __android_log_print(ANDROID_LOG_INFO, "QEMU_LOG", "Log pipe closed. QEMU process terminated.");
}


JNIEXPORT jint JNICALL
JNI_VM_FUNCTION(nativeCreateAndRunVM)(JNIEnv *env, jclass, jstring vmName_j, jobjectArray command_j, jstring dataPath_j) {
    const char* vmName_c = env->GetStringUTFChars(vmName_j, nullptr);
    std::string vmName(vmName_c);
    env->ReleaseStringUTFChars(vmName_j, vmName_c);

    const char* dataPath_c = env->GetStringUTFChars(dataPath_j, nullptr);
    const std::string libPath = std::string(dataPath_c) + "/lib";
    const std::string romPath = std::string(dataPath_c) + "/share/qemu";
    const char* linkerPath = "/system/bin/linker64";

    int sv[2];
    if (socketpair(AF_UNIX, SOCK_STREAM, 0, sv) == -1) {
        __android_log_print(ANDROID_LOG_ERROR, "QEMU_JNI", "Failed to create socketpair");
        return -1;
    }

    std::vector<std::string> commandVec;
    commandVec.push_back(linkerPath);

    int original_argc = env->GetArrayLength(command_j);
    jstring exe_j = (jstring) env->GetObjectArrayElement(command_j, 0);
    const char* exe_c = env->GetStringUTFChars(exe_j, nullptr);
    commandVec.push_back(exe_c);
    env->ReleaseStringUTFChars(exe_j, exe_c);
    env->DeleteLocalRef(exe_j);

    commandVec.push_back("-L");
    commandVec.push_back(romPath);
    commandVec.push_back("-netdev");
    commandVec.push_back("user,id=net0");
    commandVec.push_back("-device");
    commandVec.push_back("e1000,netdev=net0");

    commandVec.push_back("-chardev");
    commandVec.push_back("socket,id=char0,fd=" + std::to_string(sv[1]));
    commandVec.push_back("-serial");
    commandVec.push_back("chardev:char0");

    for (int i = 1; i < original_argc; i++) {
        jstring string_j = (jstring) env->GetObjectArrayElement(command_j, i);
        const char* string_c = env->GetStringUTFChars(string_j, nullptr);
        commandVec.push_back(string_c);
        env->ReleaseStringUTFChars(string_j, string_c);
        env->DeleteLocalRef(string_j);
    }

    char** argv = new char*[commandVec.size() + 1];
    for (size_t i = 0; i < commandVec.size(); ++i) {
        argv[i] = strdup(commandVec[i].c_str());
    }
    argv[commandVec.size()] = NULL;

    int log_pipe_fds[2];
    if (pipe(log_pipe_fds) == -1) { return -1; }

    pid_t pid = fork();

    if (pid == -1) {
        return -1;
    } else if (pid == 0) {
        close(log_pipe_fds[0]);
        close(sv[0]);

        dup2(log_pipe_fds[1], STDOUT_FILENO);
        dup2(log_pipe_fds[1], STDERR_FILENO);


        setenv("LD_LIBRARY_PATH", libPath.c_str(), 1);
        execv(linkerPath, argv);
        _exit(127);
    } else {
        close(log_pipe_fds[1]);
        close(sv[1]);

        __android_log_print(ANDROID_LOG_INFO, "VMManager", "Successfully forked process for VM '%s' with PID %d", vmName.c_str(), pid);

        std::lock_guard<std::mutex> lock(g_VmMutex);
        g_RunningVMs[vmName] = pid;

        {
            std::lock_guard<std::mutex> input_lock(g_VmInputMutex);
            g_VmInputFds[vmName] = sv[0];
        }

        std::thread log_thread(log_pipe_thread, log_pipe_fds[0]);
        log_thread.detach();

        std::thread output_thread(vm_output_thread, sv[0], vmName);
        output_thread.detach();

        for (size_t i = 0; i < commandVec.size(); ++i) {
            free(argv[i]);
        }
        delete[] argv;
        env->ReleaseStringUTFChars(dataPath_j, dataPath_c);

        return pid;
    }
}

JNIEXPORT void JNICALL
Java_org_catrobat_catroid_virtualmachine_VirtualMachineManager_nativeSendInputToVM(JNIEnv *env, jclass, jstring vmName_j, jstring input_j) {
    const char* vmName_c = env->GetStringUTFChars(vmName_j, nullptr);
    std::string vmName(vmName_c);

    int write_fd = -1;
    {
        std::lock_guard<std::mutex> lock(g_VmInputMutex);
        auto it = g_VmInputFds.find(vmName);
        if (it != g_VmInputFds.end()) {
            write_fd = it->second;
        }
    }

    if (write_fd != -1) {
        const char* input_c = env->GetStringUTFChars(input_j, nullptr);
        write(write_fd, input_c, strlen(input_c));
        env->ReleaseStringUTFChars(input_j, input_c);
    }

    env->ReleaseStringUTFChars(vmName_j, vmName_c);
}

JNIEXPORT jint JNICALL
JNI_VM_FUNCTION(nativeStopVM)(JNIEnv *env, jclass, jstring vmName_j) {
    const char* vmName_c = env->GetStringUTFChars(vmName_j, nullptr);
    std::string vmName(vmName_c);

    {
        std::lock_guard<std::mutex> lock(g_VmInputMutex);
        auto it = g_VmInputFds.find(vmName);
        if (it != g_VmInputFds.end()) {
            close(it->second);
            g_VmInputFds.erase(it);
        }
    }

    std::lock_guard<std::mutex> lock(g_VmMutex);
    auto it = g_RunningVMs.find(vmName);
    if (it != g_RunningVMs.end()) {
        pid_t pid = it->second;
        g_RunningVMs.erase(it);
        env->ReleaseStringUTFChars(vmName_j, vmName_c);

        int result = kill(pid, SIGTERM);
        if (result != 0) {
            __android_log_print(ANDROID_LOG_WARN, "VMManager", "Failed to send SIGTERM to PID %d. Trying SIGKILL.", pid);
            return kill(pid, SIGKILL);
        }
        return result;
    } else {
        __android_log_print(ANDROID_LOG_WARN, "VMManager", "VM '%s' not found in running processes map.", vmName.c_str());
        env->ReleaseStringUTFChars(vmName_j, vmName_c);
        return -1;
    }
}

} // extern "C"


extern "C" {
#define JNI_ML(name) Java_org_catrobat_catroid_ml_MLBridge_##name


JNIEXPORT void JNICALL
JNI_ML(nativeCreateRandomTensor)(JNIEnv *env,  jclass clazz, jstring name_j, jintArray shape_j, jboolean trainable) {
    const char* name = env->GetStringUTFChars(name_j, nullptr);
    jint* s_ptr = env->GetIntArrayElements(shape_j, nullptr);
    int dim = env->GetArrayLength(shape_j);
    std::vector<int> shape;
    int total_size = 1;
    for(int i=0; i<dim; i++) { shape.push_back(s_ptr[i]); total_size *= s_ptr[i]; }

    auto t = std::make_shared<CatroidTensor>(shape, 0.0f, (bool)trainable);


    std::default_random_engine generator(std::random_device{}());
    float limit = sqrt(6.0f / total_size);
    std::uniform_real_distribution<float> distribution(-limit, limit);
    for(int i=0; i<total_size; i++) t->data[i] = distribution(generator);

    std::lock_guard<std::mutex> lock(g_MnnMutex);
    g_Tensors[name] = t;
    env->ReleaseIntArrayElements(shape_j, s_ptr, 0);
    env->ReleaseStringUTFChars(name_j, name);
}

JNIEXPORT void JNICALL
JNI_ML(nativeCreateTensor)(JNIEnv *env, jclass clazz, jstring name_j, jintArray shape_j, jfloat val, jboolean trainable) {
    const char* name = env->GetStringUTFChars(name_j, nullptr);
    jint* s_ptr = env->GetIntArrayElements(shape_j, nullptr);
    int dim_count = env->GetArrayLength(shape_j);

    std::vector<int> shape;
    for(int i=0; i<dim_count; i++) shape.push_back((int)s_ptr[i]);

    std::lock_guard<std::mutex> lock(g_MnnMutex);
    g_Tensors[name] = std::make_shared<CatroidTensor>(shape, val, (bool)trainable);

    env->ReleaseIntArrayElements(shape_j, s_ptr, 0);
    env->ReleaseStringUTFChars(name_j, name);
}

JNIEXPORT jfloat JNICALL
JNI_ML(nativeGetTensorValueByIndex)(JNIEnv *env, jclass clazz, jstring name_j, jint index) {
    const char* name = env->GetStringUTFChars(name_j, nullptr);
    std::lock_guard<std::mutex> lock(g_MnnMutex);
    float res = 0.0f;
    if (g_Tensors.count(name)) {
        auto t = g_Tensors[name];
        if (index >= 0 && index < t->total_size) res = t->data[index];
    }
    env->ReleaseStringUTFChars(name_j, name);
    return res;
}

JNIEXPORT jint JNICALL
JNI_ML(nativeArgMax)(JNIEnv *env, jclass clazz, jstring name_j) {
    const char* name = env->GetStringUTFChars(name_j, nullptr);
    std::lock_guard<std::mutex> lock(g_MnnMutex);
    int res = -1;
    if (g_Tensors.count(name)) {
        auto t = g_Tensors[name];
        auto it = std::max_element(t->data.begin(), t->data.end());
        res = std::distance(t->data.begin(), it);
    }
    env->ReleaseStringUTFChars(name_j, name);
    return res;
}



JNIEXPORT void JNICALL JNI_ML(nativeReshape)(JNIEnv* env, jclass, jstring name_j, jintArray new_shape_j) {
    const char* name = env->GetStringUTFChars(name_j, nullptr);
    std::lock_guard<std::mutex> lock(g_MnnMutex);
    if (g_Tensors.count(name)) {
        auto t = g_Tensors[name];
        jint* s_ptr = env->GetIntArrayElements(new_shape_j, nullptr);
        int dim_count = env->GetArrayLength(new_shape_j);
        std::vector<int> new_shape;
        int new_size = 1;
        for(int i=0; i<dim_count; i++) {
            new_shape.push_back(s_ptr[i]);
            new_size *= s_ptr[i];
        }
        if (new_size == t->total_size) {
            auto R = std::make_shared<CatroidTensor>(new_shape, 0.0f, t->trainable, name);
            R->data = t->data;
            g_Tensors[name] = R;
            if (g_IsTraining) g_Tape.push_back({"reshape", t, nullptr, R});
        }
        env->ReleaseIntArrayElements(new_shape_j, s_ptr, 0);
    }
    env->ReleaseStringUTFChars(name_j, name);
}


JNIEXPORT void JNICALL
JNI_ML(nativeLayerLinear)(JNIEnv *env, jclass, jstring name_j, jstring in_j, jstring out_j, jint in_f, jint out_f) {
    const char* name = env->GetStringUTFChars(name_j, nullptr);
    const char* in_n = env->GetStringUTFChars(in_j, nullptr);
    const char* out_n = env->GetStringUTFChars(out_j, nullptr);

    std::lock_guard<std::mutex> lock(g_MnnMutex);
    if (g_Tensors.count(in_n)) {
        exec_layer_linear(name, in_n, out_n, in_f, out_f);
    }

    env->ReleaseStringUTFChars(name_j, name);
    env->ReleaseStringUTFChars(in_j, in_n);
    env->ReleaseStringUTFChars(out_j, out_n);
}

JNIEXPORT void JNICALL JNI_ML(nativeOp)(JNIEnv *env, jclass, jstring res_j, jstring a_j, jstring b_j, jstring op_j) {
    const char* res_n = env->GetStringUTFChars(res_j, nullptr);
    const char* a_n = env->GetStringUTFChars(a_j, nullptr);
    const char* b_n = (b_j) ? env->GetStringUTFChars(b_j, nullptr) : nullptr;
    const char* op_type_c = env->GetStringUTFChars(op_j, nullptr);
    std::string op_type(op_type_c);

    std::lock_guard<std::mutex> lock(g_MnnMutex);

    if (!g_Tensors.count(a_n)) return;
    auto A = g_Tensors[a_n];
    auto B = (b_n && g_Tensors.count(b_n)) ? g_Tensors[b_n] : nullptr;

    std::shared_ptr<CatroidTensor> R;
    bool is_trainable = g_IsTraining && (A->trainable || (B && B->trainable));

    if (op_type == "add") { R = std::make_shared<CatroidTensor>(A->shape, 0.0f, is_trainable, res_n); kernel_add_forward(A, B, R); }
    else if (op_type == "sub") { R = std::make_shared<CatroidTensor>(A->shape, 0.0f, is_trainable, res_n); kernel_sub_forward(A, B, R); }
    else if (op_type == "mul") { R = std::make_shared<CatroidTensor>(A->shape, 0.0f, is_trainable, res_n); kernel_mul_forward(A, B, R); }
    else if (op_type == "matmul") { std::vector<int> res_shape = {A->shape[0], B->shape[1]}; R = std::make_shared<CatroidTensor>(res_shape, 0.0f, is_trainable, res_n); kernel_matmul_forward(A, B, R); }
    else if (op_type == "relu") { R = std::make_shared<CatroidTensor>(A->shape, 0.0f, is_trainable, res_n); kernel_relu_forward(A, R); }
    else if (op_type == "sigmoid") { R = std::make_shared<CatroidTensor>(A->shape, 0.0f, is_trainable, res_n); kernel_sigmoid_forward(A, R); }
    else if (op_type == "tanh") { R = std::make_shared<CatroidTensor>(A->shape, 0.0f, is_trainable, res_n); kernel_tanh_forward(A, R); }
    else if (op_type == "softmax") { R = std::make_shared<CatroidTensor>(A->shape, 0.0f, is_trainable, res_n); kernel_softmax_forward(A, R); }
    else if (op_type == "mse_loss") { R = std::make_shared<CatroidTensor>(std::vector<int>{1}, 0.0f, is_trainable, res_n); kernel_mse_loss_forward(A, B, R); }
    else if (op_type == "sum") { R = std::make_shared<CatroidTensor>(std::vector<int>{1}, 0.0f, is_trainable, res_n); kernel_sum_forward(A, R); }

    if (R) {
        g_Tensors[res_n] = R;
        if (g_IsTraining) g_Tape.push_back({op_type, A, B, R});
    }

    env->ReleaseStringUTFChars(res_j, res_n); env->ReleaseStringUTFChars(a_j, a_n);
    if (b_j) env->ReleaseStringUTFChars(b_j, b_n); env->ReleaseStringUTFChars(op_j, op_type_c);
}


JNIEXPORT void JNICALL JNI_ML(nativeBackward)(JNIEnv *env, jclass, jstring loss_j) {
    const char* loss_n = env->GetStringUTFChars(loss_j, nullptr);
    std::lock_guard<std::mutex> lock(g_MnnMutex);

    if(!g_Tensors.count(loss_n)) { env->ReleaseStringUTFChars(loss_j, loss_n); return; }

    for (auto const& [name, t] : g_Tensors) t->zero_grad();

    auto L = g_Tensors[loss_n];
    L->grad.assign(L->total_size, 1.0f);

    for (int i = (int)g_Tape.size() - 1; i >= 0; i--) {
        auto& node = g_Tape[i];
        if (node.op_type == "add") kernel_add_backward(node);
        else if (node.op_type == "sub") kernel_sub_backward(node);
        else if (node.op_type == "mul") kernel_mul_backward(node);
        else if (node.op_type == "matmul") kernel_matmul_backward(node);
        else if (node.op_type == "relu") kernel_relu_backward(node);
        else if (node.op_type == "sigmoid") kernel_sigmoid_backward(node);
        else if (node.op_type == "tanh") kernel_tanh_backward(node);
        else if (node.op_type == "softmax") kernel_softmax_backward(node);
        else if (node.op_type == "mse_loss") kernel_mse_loss_backward(node);
        else if (node.op_type == "sum") kernel_sum_backward(node);
        else if (node.op_type == "reshape") {
            if (node.input_a->trainable) for(int j=0; j<node.output->total_size; j++) node.input_a->grad[j] += node.output->grad[j];
        }
    }
    env->ReleaseStringUTFChars(loss_j, loss_n);
}


JNIEXPORT void JNICALL
JNI_ML(nativeSetTensor)(JNIEnv *env, jclass clazz, jstring name_j, jstring data_j) {
    const char* name = env->GetStringUTFChars(name_j, nullptr);
    const char* data_raw = env->GetStringUTFChars(data_j, nullptr);
    std::string s = data_raw;

    std::lock_guard<std::mutex> lock(g_MnnMutex);
    if (g_Tensors.count(name)) {
        auto t = g_Tensors[name];

        std::replace(s.begin(), s.end(), '\n', ',');
        std::replace(s.begin(), s.end(), ';', ',');

        std::stringstream ss(s);
        std::string item;
        int i = 0;
        while (std::getline(ss, item, ',') && i < t->total_size) {
            try {
                if (!item.empty()) t->data[i++] = std::stof(item);
            } catch (...) {}
        }
    }
    env->ReleaseStringUTFChars(name_j, name);
    env->ReleaseStringUTFChars(data_j, data_raw);
}


JNIEXPORT void JNICALL
JNI_ML(nativeSetTensorByIndex)(JNIEnv *env, jclass clazz, jstring name_j, jint index, jfloat value) {
    const char* name = env->GetStringUTFChars(name_j, nullptr);
    std::lock_guard<std::mutex> lock(g_MnnMutex);
    if (g_Tensors.count(name)) {
        auto t = g_Tensors[name];
        if (index >= 0 && index < t->total_size) t->data[index] = value;
    }
    env->ReleaseStringUTFChars(name_j, name);
}


JNIEXPORT void JNICALL JNI_ML(nativeStep)(JNIEnv*, jclass, jfloat lr) {
    std::lock_guard<std::mutex> lock(g_MnnMutex);
    for (auto const& [name, t] : g_Tensors) {
        if (t->trainable) for (int i=0; i < t->total_size; i++) t->data[i] -= lr * t->grad[i];
    }
    g_Tape.clear();
}


JNIEXPORT jfloatArray JNICALL
JNI_ML(nativeGetTensor)(JNIEnv *env, jclass clazz, jstring name_j) {
    const char* name = env->GetStringUTFChars(name_j, nullptr);
    std::lock_guard<std::mutex> lock(g_MnnMutex);
    if (!g_Tensors.count(name)) return nullptr;

    auto t = g_Tensors[name];
    jfloatArray result = env->NewFloatArray(t->total_size);
    env->SetFloatArrayRegion(result, 0, t->total_size, t->data.data());
    env->ReleaseStringUTFChars(name_j, name);
    return result;
}


JNIEXPORT jstring JNICALL
JNI_ML(nativeGetTensorAsString)(JNIEnv *env, jclass clazz, jstring name_j) {
    const char* name = env->GetStringUTFChars(name_j, nullptr);
    std::lock_guard<std::mutex> lock(g_MnnMutex);
    if (!g_Tensors.count(name)) return env->NewStringUTF("");

    auto t = g_Tensors[name];
    std::stringstream ss;
    int last_dim = t->shape.back();

    for (int i = 0; i < t->total_size; i++) {
        ss << t->data[i];
        if ((i + 1) % last_dim == 0) {
            if (i != t->total_size - 1) ss << "\n";
        } else {
            ss << ",";
        }
    }
    env->ReleaseStringUTFChars(name_j, name);
    return env->NewStringUTF(ss.str().c_str());
}

JNIEXPORT jstring JNICALL
JNI_ML(nativeGetTensorFormatted)(JNIEnv *env, jobject, jstring name_j) {
    const char* name = env->GetStringUTFChars(name_j, nullptr);
    std::lock_guard<std::mutex> lock(g_MnnMutex);

    if (!g_Tensors.count(name)) return env->NewStringUTF("");
    auto t = g_Tensors[name];

    std::stringstream ss;
    int last_dim = t->shape.back();

    for (int i = 0; i < t->total_size; i++) {
        ss << t->data[i];
        if ((i + 1) % last_dim == 0) {
            if (i != t->total_size - 1) ss << "\n";
        } else {
            ss << ",";
        }
    }

    env->ReleaseStringUTFChars(name_j, name);
    return env->NewStringUTF(ss.str().c_str());
}

JNIEXPORT jstring JNICALL
JNI_ML(nativeGetShape)(JNIEnv *env, jclass clazz, jstring name_j) {
    const char* name = env->GetStringUTFChars(name_j, nullptr);
    std::lock_guard<std::mutex> lock(g_MnnMutex);

    if (!g_Tensors.count(name)) return env->NewStringUTF("none");

    auto t = g_Tensors[name];
    std::string res = "";
    for(int i=0; i < t->shape.size(); i++) {
        res += std::to_string(t->shape[i]) + (i == t->shape.size()-1 ? "" : ",");
    }

    env->ReleaseStringUTFChars(name_j, name);
    return env->NewStringUTF(res.c_str());
}


JNIEXPORT jfloat JNICALL
JNI_ML(nativeGetValueND)(JNIEnv *env, jclass clazz, jstring name_j, jstring indices_j) {
    const char* name = env->GetStringUTFChars(name_j, nullptr);
    const char* idx_str = env->GetStringUTFChars(indices_j, nullptr);

    std::lock_guard<std::mutex> lock(g_MnnMutex);
    if (!g_Tensors.count(name)) return 0.0f;

    auto t = g_Tensors[name];
    std::stringstream ss(idx_str);
    std::string item;
    std::vector<int> coords;
    while (std::getline(ss, item, ',')) coords.push_back(std::stoi(item));


    int flat_idx = 0;
    int multiplier = 1;
    for (int i = (int)t->shape.size() - 1; i >= 0; i--) {
        if (i < coords.size()) flat_idx += coords[i] * multiplier;
        multiplier *= t->shape[i];
    }

    env->ReleaseStringUTFChars(name_j, name);
    env->ReleaseStringUTFChars(indices_j, idx_str);

    if (flat_idx >= 0 && flat_idx < t->total_size) return t->data[flat_idx];
    return 0.0f;
}

JNIEXPORT jint JNICALL JNI_ML(nativeGetTotalSize)(JNIEnv *env, jclass clazz, jstring name_j) {
    if (name_j == nullptr) return -1;

    const char* name = env->GetStringUTFChars(name_j, nullptr);
    if (name == nullptr) return -1;

    jint result = -1;
    {
        std::lock_guard<std::mutex> lock(g_MnnMutex);
        if (g_Tensors.count(name)) {
            result = g_Tensors[name]->total_size;
        }
    }

    env->ReleaseStringUTFChars(name_j, name);
    return result;
}

JNIEXPORT void JNICALL
JNI_ML(nativeSetTrainingMode)(JNIEnv *env, jclass, jboolean mode) {
    std::lock_guard<std::mutex> lock(g_MnnMutex);
    g_IsTraining = mode;
    if (!mode) {
        g_Tape.clear();
        g_Tape.shrink_to_fit();
    }
}

extern "C" JNIEXPORT void JNICALL
JNI_ML(nativeSaveModel)(JNIEnv *env, jclass, jstring path_j) {
    const char* path = env->GetStringUTFChars(path_j, nullptr);
    std::ofstream fs(path, std::ios::binary);
    if (!fs.is_open()) return;

    std::lock_guard<std::mutex> lock(g_MnnMutex);
    int count = 0;
    for (auto const& [name, t] : g_Tensors) if (t->trainable) count++;

    fs.write((char*)&count, sizeof(int));
    __android_log_print(ANDROID_LOG_INFO, "Pocketensor", "SAVE: Начало записи %d тензоров в %s", count, path);
    for (auto const& [name, t] : g_Tensors) {
        if (!t->trainable) continue;
        float first_val = (t->total_size > 0) ? t->data[0] : 0.0f;

        if (std::isnan(first_val) || std::isinf(first_val)) {
            __android_log_print(ANDROID_LOG_ERROR, "Pocketensor", "SAVE WARNING: Тензор '%s' сломан (NaN/Inf)!", name.c_str());
        } else {
            __android_log_print(ANDROID_LOG_INFO, "Pocketensor", "SAVE: Запись '%s' [%d], val[0]=%f", name.c_str(), t->total_size, first_val);
        }
        int name_len = name.size();
        fs.write((char*)&name_len, sizeof(int));
        fs.write(name.c_str(), name_len);
        int dims = t->shape.size();
        fs.write((char*)&dims, sizeof(int));
        fs.write((char*)t->shape.data(), sizeof(int) * dims);
        fs.write((char*)t->data.data(), sizeof(float) * t->total_size);
    }
    fs.close();
    env->ReleaseStringUTFChars(path_j, path);
}

JNIEXPORT void JNICALL
Java_org_catrobat_catroid_ml_MLBridge_nativeStepAdam(JNIEnv *env, jclass, jfloat lr) {
    std::lock_guard<std::mutex> lock(g_MnnMutex);


    const float beta1 = 0.9f;
    const float beta2 = 0.999f;
    const float eps = 1e-8f;

    for (auto const& [name, t] : g_Tensors) {
        if (!t->trainable) continue;


        if (g_AdamStates.find(name) == g_AdamStates.end()) {
            g_AdamStates[name].m.assign(t->total_size, 0.0f);
            g_AdamStates[name].v.assign(t->total_size, 0.0f);
            g_AdamStates[name].t = 0;
        }

        AdamState& st = g_AdamStates[name];
        st.t++;

        float bias_correction1 = 1.0f - std::pow(beta1, st.t);
        float bias_correction2 = 1.0f - std::pow(beta2, st.t);

        for (int i = 0; i < t->total_size; i++) {
            float grad = t->grad[i];


            if (grad > 5.0f) grad = 5.0f;
            if (grad < -5.0f) grad = -5.0f;


            st.m[i] = beta1 * st.m[i] + (1.0f - beta1) * grad;
            st.v[i] = beta2 * st.v[i] + (1.0f - beta2) * grad * grad;


            float m_hat = st.m[i] / bias_correction1;
            float v_hat = st.v[i] / bias_correction2;

            t->data[i] -= lr * m_hat / (std::sqrt(v_hat) + eps);


            t->grad[i] = 0.0f;
        }
    }
    g_Tape.clear();
}

JNIEXPORT jboolean JNICALL
Java_org_catrobat_catroid_ml_MLBridge_nativeLoadModel(JNIEnv *env, jclass, jstring path_j) {
    const char* path = env->GetStringUTFChars(path_j, nullptr);
    std::ifstream fs(path, std::ios::binary);
    if (!fs.is_open()) {
        __android_log_print(ANDROID_LOG_ERROR, "Pocketensor", "LOAD: Не удалось открыть файл %s", path);
        env->ReleaseStringUTFChars(path_j, path);
        return JNI_FALSE;
    }

    int tensor_count;
    fs.read((char*)&tensor_count, sizeof(int));
    __android_log_print(ANDROID_LOG_DEBUG, "Pocketensor", "LOAD: В файле найдено тензоров: %d", tensor_count);

    std::lock_guard<std::mutex> lock(g_MnnMutex);

    for (int i = 0; i < tensor_count; i++) {
        int name_len;
        fs.read((char*)&name_len, sizeof(int));
        std::vector<char> name_buf(name_len);
        fs.read(name_buf.data(), name_len);
        std::string name(name_buf.begin(), name_buf.end());

        int dims_count;
        fs.read((char*)&dims_count, sizeof(int));
        std::vector<int> shape(dims_count);
        fs.read((char*)shape.data(), sizeof(int) * dims_count);


        int size = 1;
        for (int d : shape) size *= d;
        std::vector<float> data(size);
        fs.read((char*)data.data(), sizeof(float) * size);


        if (g_Tensors.count(name)) {
            auto& target = g_Tensors[name];

            if (target->total_size == size) {
                target->data = data;
                __android_log_print(ANDROID_LOG_INFO, "Pocketensor", "LOAD: Успешно загружен тензор: %s", name.c_str());
            } else {
                __android_log_print(ANDROID_LOG_ERROR, "Pocketensor", "LOAD: Ошибка размера для %s! В файле: %d, В проекте: %d", name.c_str(), size, target->total_size);
            };
        } else {

            auto t = std::make_shared<CatroidTensor>(shape, 0.0f, true, name);
            t->data = data;
            g_Tensors[name] = t;
            __android_log_print(ANDROID_LOG_INFO, "Pocketensor", "LOAD: Создан новый тензор из файла: %s", name.c_str());
        }
    }

    fs.close();
    env->ReleaseStringUTFChars(path_j, path);
    return JNI_TRUE;
}


}
#if USE_MNN
#include "MNN/llm.hpp"
#include <sstream>
#include <chrono>

static std::unique_ptr<MNN::Transformer::Llm> g_kove_llm = nullptr;
static bool g_kove_loaded = false;
#endif

std::string jstring_to_utf8(JNIEnv* env, jstring j_str) {
    if (!j_str) return "";

    jclass string_class = env->FindClass("java/lang/String");
    jmethodID get_bytes_method = env->GetMethodID(string_class, "getBytes", "(Ljava/lang/String;)[B");
    jstring utf8_charset = env->NewStringUTF("UTF-8");

    auto byte_array = (jbyteArray)env->CallObjectMethod(j_str, get_bytes_method, utf8_charset);
    jsize len = env->GetArrayLength(byte_array);
    jbyte* bytes = env->GetByteArrayElements(byte_array, nullptr);

    std::string result(reinterpret_cast<char*>(bytes), len);

    env->ReleaseByteArrayElements(byte_array, bytes, JNI_ABORT);
    env->DeleteLocalRef(utf8_charset);
    env->DeleteLocalRef(byte_array);
    env->DeleteLocalRef(string_class);

    return result;
}

extern "C" {

JNIEXPORT jboolean JNICALL
Java_org_catrobat_catroid_ai_KoveManager_nativeInitKove(JNIEnv* env, jclass clazz, jstring j_model_path) {
#if USE_MNN
    if (g_kove_loaded) {
        __android_log_print(ANDROID_LOG_INFO, "KOVE_NATIVE", "Kove is already initialized.");
        return JNI_TRUE;
    }

    std::string model_path = jstring_to_utf8(env, j_model_path);
    if (model_path.empty()) {
        __android_log_print(ANDROID_LOG_ERROR, "KOVE_NATIVE", "Failed to convert model path.");
        return JNI_FALSE;
    }

    std::string config_path = model_path + "/config.json";
    __android_log_print(ANDROID_LOG_INFO, "KOVE_NATIVE", "Kove JNI started with config: %s", config_path.c_str());

    try {
        g_kove_llm.reset(MNN::Transformer::Llm::createLLM(config_path));
        if (!g_kove_llm) {
            __android_log_print(ANDROID_LOG_ERROR, "KOVE_NATIVE", "MNN Llm::createLLM returned nullptr!");
            return JNI_FALSE;
        }

        __android_log_print(ANDROID_LOG_INFO, "KOVE_BENCHMARK", "Starting model weights loading...");
        auto start_load = std::chrono::high_resolution_clock::now();

        g_kove_llm->load();

        auto end_load = std::chrono::high_resolution_clock::now();
        auto load_duration = std::chrono::duration_cast<std::chrono::milliseconds>(end_load - start_load).count();
        __android_log_print(ANDROID_LOG_INFO, "KOVE_BENCHMARK", "[SUCCESS] Kove weights loaded in %lld ms!", load_duration);

        g_kove_loaded = true;

        __android_log_print(ANDROID_LOG_INFO, "KOVE_BENCHMARK", "Starting startup self-test generation...");
        std::string test_prompt = u8"<|fim_prefix|>@Sprite(\"Фон\")\nclass Фон:\n    looks = ['вкл', 'выкл']\n    sounds = []\n\n    @WhenScript\n    def start():<|fim_suffix|>\n            next_look()<|fim_middle|>";

        g_kove_llm->reset();

        auto start_gen = std::chrono::high_resolution_clock::now();

        std::stringstream test_response_buffer;
        g_kove_llm->response(test_prompt, &test_response_buffer);

        auto end_gen = std::chrono::high_resolution_clock::now();
        auto gen_duration = std::chrono::duration_cast<std::chrono::milliseconds>(end_gen - start_gen).count();

        std::string response = test_response_buffer.str();
        if (response.rfind(test_prompt, 0) == 0) {
            response = response.substr(test_prompt.length());
        }

        double speed_chars_sec = (response.length() > 0 && gen_duration > 0)
                                 ? (static_cast<double>(response.length()) / (gen_duration / 1000.0))
                                 : 0.0;

        double est_tokens_sec = speed_chars_sec / 4.0;

        __android_log_print(ANDROID_LOG_INFO, "KOVE_BENCHMARK", "Generation complete in %lld ms!", gen_duration);
        __android_log_print(ANDROID_LOG_INFO, "KOVE_BENCHMARK", "Output length: %zu chars. Estimated speed: %.2f tokens/sec", response.length(), est_tokens_sec);
        __android_log_print(ANDROID_LOG_INFO, "KOVE_NATIVE", "Kove Startup Test Output:\n%s", response.c_str());

    } catch (const std::exception& e) {
        __android_log_print(ANDROID_LOG_ERROR, "KOVE_NATIVE", "Exception during Kove JNI init: %s", e.what());
        g_kove_loaded = false;
        g_kove_llm.reset();
    } catch (...) {
        __android_log_print(ANDROID_LOG_ERROR, "KOVE_NATIVE", "Unknown error during Kove JNI init.");
        g_kove_loaded = false;
        g_kove_llm.reset();
    }

    return g_kove_loaded ? JNI_TRUE : JNI_FALSE;
#else
#endif
}

JNIEXPORT jstring JNICALL
Java_org_catrobat_catroid_ai_KoveManager_nativeCompleteKove(JNIEnv* env, jclass clazz, jstring j_prompt) {
#if USE_MNN
    if (!g_kove_loaded || !g_kove_llm) {
        __android_log_print(ANDROID_LOG_ERROR, "KOVE_NATIVE", "Kove is not loaded.");
        return env->NewStringUTF("ERROR: Model not initialized");
    }

    std::string prompt = jstring_to_utf8(env, j_prompt);
    if (prompt.empty()) {
        return env->NewStringUTF("ERROR: Empty or corrupted prompt");
    }

    try {
        g_kove_llm->reset();

        auto start_gen = std::chrono::high_resolution_clock::now();

        std::stringstream response_buffer;
        g_kove_llm->response(prompt, &response_buffer);

        auto end_gen = std::chrono::high_resolution_clock::now();
        auto gen_duration = std::chrono::duration_cast<std::chrono::milliseconds>(end_gen - start_gen).count();

        std::string response = response_buffer.str();
        if (response.rfind(prompt, 0) == 0) {
            response = response.substr(prompt.length());
        }

        double speed_chars_sec = (response.length() > 0 && gen_duration > 0)
                                 ? (static_cast<double>(response.length()) / (gen_duration / 1000.0))
                                 : 0.0;
        double est_tokens_sec = speed_chars_sec / 4.0;

        __android_log_print(ANDROID_LOG_INFO, "KOVE_BENCHMARK", "Inference complete in %lld ms. Speed: %.2f tokens/sec", gen_duration, est_tokens_sec);

        return env->NewStringUTF(response.c_str());
    } catch (const std::exception& e) {
        __android_log_print(ANDROID_LOG_ERROR, "KOVE_NATIVE", "Exception during Kove completion: %s", e.what());
        return env->NewStringUTF("ERROR: Exception during inference");
    }
#else
#endif
}

} // extern "C"
