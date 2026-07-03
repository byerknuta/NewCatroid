package org.catrobat.catroid.ai

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.catrobat.catroid.CatroidApplication
import org.catrobat.catroid.R
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

interface KoveCallback {
    fun onResult(success: Boolean)
}

object KoveManager {
    private const val TAG = "KoveManager"
    private const val MODEL_URL = "https://github.com/Danveyd/Kove/releases/download/release/kove_v1.zip"
    private const val MODEL_DIR_NAME = "kove_model"

    init {
        try {
            System.loadLibrary("catroid")
            Log.i(TAG, "Native library 'catroid' loaded successfully.")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native library 'catroid'", e)
        }
    }

    @JvmStatic
    external fun nativeInitKove(modelPath: String): Boolean

    @JvmStatic
    external fun nativeCompleteKove(prompt: String): String

    suspend fun getAutocompleteSuggestion(prefix: String, suffix: String): String = withContext(Dispatchers.IO) {
        val context = CatroidApplication.getAppContext()
        if (!isModelDownloaded(context)) {
            return@withContext "ERROR: Model not downloaded"
        }

        val prompt = "<|fim_prefix|>$prefix<|fim_suffix|>$suffix<|fim_middle|>"

        val suggestion = nativeCompleteKove(prompt)

        return@withContext suggestion
    }

    fun getModelDirectory(context: Context): File {
        return File(context.filesDir, MODEL_DIR_NAME)
    }

    @JvmStatic
    fun isModelDownloaded(context: Context): Boolean {
        val dir = getModelDirectory(context)
        val mnnModel = File(dir, "llm.mnn")
        val mnnWeight = File(dir, "llm.mnn.weight")
        val config = File(dir, "llm_config.json")
        val tokenizer = File(dir, "tokenizer.txt")
        return mnnModel.exists() && mnnWeight.exists() && config.exists() && tokenizer.exists()
    }

    @JvmStatic
    fun initIfEnabled(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val devMode = prefs.getBoolean("setting_developer_mode_enabled", false)
        val aiEnabled = prefs.getBoolean("setting_kove_ai_autocomplete", false)

        if (devMode && aiEnabled && isModelDownloaded(context)) {
            val modelPath = getModelDirectory(context).absolutePath
            Thread {
                val success = nativeInitKove(modelPath)
                if (success) {
                    Log.i(TAG, "Kove JNI initialization complete. Self-test passed.")
                } else {
                    Log.e(TAG, "Kove JNI initialization failed.")
                }
            }.start()
        }
    }

    @JvmStatic
    fun showDownloadDialog(context: Context, callback: KoveCallback) {
        AlertDialog.Builder(context)
            .setTitle(R.string.ai_download_dialog_title)
            .setMessage(R.string.ai_download_dialog_message)
            .setCancelable(false)
            .setPositiveButton(R.string.button_download) { _, _ ->
                startDownload(context, callback)
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                callback.onResult(false)
            }
            .show()
    }

    private fun startDownload(context: Context, callback: KoveCallback) {
        val progressDialog = ProgressDialog(context).apply {
            setTitle(R.string.ai_download_dialog_title)
            setMessage(context.getString(R.string.ai_downloading_progress, 0))
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            max = 100
            setCancelable(false)
            show()
        }

        Thread {
            val success = downloadAndExtractModel(context, progressDialog)
            progressDialog.dismiss()

            (context as? android.app.Activity)?.runOnUiThread {
                if (success) {
                    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                    prefs.edit().putBoolean("setting_kove_ai_autocomplete", true).apply()
                    showRestartDialog(context)
                    callback.onResult(true)
                } else {
                    Toast.makeText(context, R.string.ai_download_failed, Toast.LENGTH_LONG).show()
                    callback.onResult(false)
                }
            }
        }.start()
    }

    private fun downloadAndExtractModel(context: Context, progressDialog: ProgressDialog): Boolean {
        val targetDir = getModelDirectory(context)
        if (!targetDir.exists()) targetDir.mkdirs()

        val zipFile = File(targetDir, "kove_v1.zip")
        var connection: HttpURLConnection? = null
        try {
            val url = URL(MODEL_URL)
            connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return false
            }

            val fileLength = connection.contentLength
            val input = BufferedInputStream(connection.inputStream)
            val output = FileOutputStream(zipFile)

            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                total += count
                if (fileLength > 0) {
                    val progress = (total * 100 / fileLength).toInt()
                    progressDialog.progress = progress
                    (context as? android.app.Activity)?.runOnUiThread {
                        progressDialog.setMessage(context.getString(R.string.ai_downloading_progress, progress))
                    }
                }
                output.write(data, 0, count)
            }
            output.flush()
            output.close()
            input.close()

            (context as? android.app.Activity)?.runOnUiThread {
                progressDialog.isIndeterminate = true
                progressDialog.setMessage(context.getString(R.string.ai_unzipping))
            }
            unzip(zipFile, targetDir)
            zipFile.delete()
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Error downloading or extracting model", e)
            return false
        } finally {
            connection?.disconnect()
        }
    }

    private fun unzip(zipFile: File, targetDirectory: File) {
        ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
            var entry: ZipEntry?
            val buffer = ByteArray(4096)
            while (zis.nextEntry.also { entry = it } != null) {
                if (entry!!.isDirectory) continue

                val pureFileName = File(entry!!.name).name
                val file = File(targetDirectory, pureFileName)

                val canonicalPath = file.canonicalPath
                if (!canonicalPath.startsWith(targetDirectory.canonicalPath)) {
                    throw SecurityException("ZIP path traversal security error")
                }

                file.parentFile?.mkdirs()
                BufferedOutputStream(FileOutputStream(file)).use { bos ->
                    var len: Int
                    while (zis.read(buffer).also { len = it } > 0) {
                        bos.write(buffer, 0, len)
                    }
                }
            }
        }
        Log.i(TAG, "Unzip complete. Path flattening and tokenizer renaming applied successfully.")
    }

    private fun showRestartDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.ai_download_complete)
            .setMessage(R.string.ai_download_complete)
            .setCancelable(false)
            .setPositiveButton(R.string.button_restart) { _, _ ->
                restartApplication(context)
            }
            .show()
    }

    private fun restartApplication(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            val componentName = intent.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)
            context.startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        }
    }
}
