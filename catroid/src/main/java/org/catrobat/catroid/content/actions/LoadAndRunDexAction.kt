package org.catrobat.catroid.content.actions

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import com.badlogic.gdx.scenes.scene2d.Action
import dalvik.system.DexClassLoader
import org.catrobat.catroid.CatroidApplication
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import java.io.File
import kotlin.concurrent.thread

class LoadAndRunDexAction : Action() {
    var scope: Scope? = null
    var dexPath: Formula? = null
    var className: Formula? = null
    var methodName: Formula? = null
    var methodArg: Formula? = null

    private var started = false
    @Volatile private var finished = false

    override fun act(delta: Float): Boolean {
        if (!started) {
            started = true
            runAsyncLoad()
        }
        return finished
    }

    private fun runAsyncLoad() {
        thread(start = true) {
            try {
                val context = CatroidApplication.getAppContext()
                val projectDir = scope?.project?.getFilesDir() ?: return@thread

                val pathStr = dexPath?.interpretString(scope) ?: "classes.dex"
                val classStr = className?.interpretString(scope) ?: "game.Main"
                val methodStr = methodName?.interpretString(scope) ?: "onStart"
                val argStr = methodArg?.interpretString(scope) ?: ""

                val dexPathsList = pathStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val resolvedPaths = mutableListOf<String>()

                val processedLibsDir = File(projectDir, "libs_processed")
                val cacheDir = context.codeCacheDir
                val nativeLibsDir = File(cacheDir, "native_libs_${System.currentTimeMillis()}").apply { mkdirs() }
                if (!processedLibsDir.exists()) processedLibsDir.mkdirs()

                for (path in dexPathsList) {
                    val file = File(projectDir, path)
                    if (file.exists()) {
                        if (file.extension == "jar") {
                            val processedJar = org.catrobat.catroid.ide.ScriptExecutor.processLibrary(
                                context, file, processedLibsDir, nativeLibsDir, targetApi = 34
                            )
                            if (processedJar.exists()) {
                                resolvedPaths.add(processedJar.absolutePath)
                            }
                        } else {
                            resolvedPaths.add(file.absolutePath)
                        }
                    }
                }

                if (resolvedPaths.isEmpty()) {
                    finished = true
                    return@thread
                }

                val classpath = resolvedPaths.joinToString(File.pathSeparator)

                val classLoader = DexClassLoader(
                    classpath,
                    cacheDir.absolutePath,
                    nativeLibsDir.absolutePath,
                    context.classLoader
                )

                val clazz = classLoader.loadClass(classStr)
                val instance = clazz.newInstance()

                val methods = clazz.methods.filter { it.name == methodStr }
                if (methods.isNotEmpty()) {
                    val activity = com.badlogic.gdx.Gdx.app as? android.app.Activity

                    var methodInvoked = false
                    val signatures = listOf(
                        arrayOf(Activity::class.java, ViewGroup::class.java) to { method: java.lang.reflect.Method ->
                            val container = activity?.findViewById<ViewGroup>(android.R.id.content)
                            Handler(Looper.getMainLooper()).post {
                                try { method.invoke(instance, activity, container) } catch (e: Exception) { e.printStackTrace() }
                            }
                        },
                        arrayOf(Context::class.java, ViewGroup::class.java) to { method: java.lang.reflect.Method ->
                            val container = activity?.findViewById<ViewGroup>(android.R.id.content)
                            Handler(Looper.getMainLooper()).post {
                                try { method.invoke(instance, context, container) } catch (e: Exception) { e.printStackTrace() }
                            }
                        },
                        arrayOf(Activity::class.java) to { method: java.lang.reflect.Method ->
                            try { method.invoke(instance, activity) } catch (e: Exception) { e.printStackTrace() }
                        },
                        arrayOf(Context::class.java) to { method: java.lang.reflect.Method ->
                            try { method.invoke(instance, context) } catch (e: Exception) { e.printStackTrace() }
                        },
                        arrayOf(String::class.java) to { method: java.lang.reflect.Method ->
                            try { method.invoke(instance, argStr) } catch (e: Exception) { e.printStackTrace() }
                        },
                        emptyArray<Class<*>>() to { method: java.lang.reflect.Method ->
                            try { method.invoke(instance) } catch (e: Exception) { e.printStackTrace() }
                        }
                    )

                    for ((paramTypes, invocation) in signatures) {
                        val matchingMethod = methods.find { it.parameterTypes.contentEquals(paramTypes) }
                        if (matchingMethod != null) {
                            invocation(matchingMethod)
                            methodInvoked = true
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                finished = true
            }
        }
    }

    override fun restart() {
        super.restart()
        started = false
        finished = false
    }

    override fun reset() {
        super.reset()
        started = false
        finished = false
        scope = null
        dexPath = null
        className = null
        methodName = null
        methodArg = null
    }
}
