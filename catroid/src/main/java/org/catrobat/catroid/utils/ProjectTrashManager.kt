package org.catrobat.catroid.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import org.catrobat.catroid.R
import org.catrobat.catroid.common.FlavoredConstants.DEFAULT_ROOT_DIRECTORY
import org.catrobat.catroid.io.StorageOperations
import java.io.File
import java.io.IOException

object ProjectTrashManager {
    private const val TAG = "ProjectTrashManager"
    private const val TRASH_DIR_NAME = "projects_trash"
    private const val DELETED_SUFFIX = "_deleted_"

    @JvmStatic
    fun getTrashDirectory(context: Context): File {
        val trashDir = File(DEFAULT_ROOT_DIRECTORY, TRASH_DIR_NAME)
        if (!trashDir.exists()) {
            trashDir.mkdirs()
        }
        return trashDir
    }

    @JvmStatic
    fun getProjectsDirectory(context: Context): File {
        return DEFAULT_ROOT_DIRECTORY
    }

    @JvmStatic
    fun moveToTrash(context: Context, projectDir: File): Boolean {
        return try {
            val trashDir = getTrashDirectory(context)
            val timestamp = System.currentTimeMillis()
            val trashFolderName = projectDir.name + DELETED_SUFFIX + timestamp
            val targetDir = File(trashDir, trashFolderName)

            var success = projectDir.renameTo(targetDir)
            if (!success) {
                Log.w(TAG, "renameTo failed, using manual copy-and-delete fallback")
                StorageOperations.copyDir(projectDir, targetDir)
                StorageOperations.deleteDir(projectDir)
                success = true
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to move project to trash: ${projectDir.name}", e)
            false
        }
    }

    @JvmStatic
    fun restoreFromTrash(context: Context, trashProjectDir: File): Boolean {
        return try {
            val folderName = trashProjectDir.name
            val suffixIndex = folderName.lastIndexOf(DELETED_SUFFIX)
            if (suffixIndex == -1) return false

            val originalName = folderName.substring(0, suffixIndex)
            val projectsDir = getProjectsDirectory(context)
            var targetDir = File(projectsDir, originalName)

            var counter = 1
            var restoredName = originalName
            while (targetDir.exists()) {
                restoredName = "$originalName ($counter)"
                targetDir = File(projectsDir, restoredName)
                counter++
            }

            var success = trashProjectDir.renameTo(targetDir)
            if (!success) {
                Log.w(TAG, "renameTo failed during restore, using manual copy-and-delete fallback")
                StorageOperations.copyDir(trashProjectDir, targetDir)
                StorageOperations.deleteDir(trashProjectDir)
                success = true
            }

            if (success) {
                val codeXmlFile = File(targetDir, "code.xml")
                if (codeXmlFile.exists()) {
                    try {
                        var content = codeXmlFile.readText(Charsets.UTF_8)
                        content = content.replace(Regex("<projectName>.*?</projectName>"), "<projectName>$restoredName</projectName>")
                        codeXmlFile.writeText(content, Charsets.UTF_8)
                        Log.i(TAG, "Successfully updated internal project name in XML to: $restoredName")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to update internal project name in XML", e)
                    }
                }
            }

            success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore project from trash: ${trashProjectDir.name}", e)
            false
        }
    }

    @JvmStatic
    fun cleanExpiredTrash(context: Context) {
        Thread {
            try {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val retentionDays = prefs.getString("pref_trash_retention_days", "14")?.toIntOrNull() ?: 14
                val retentionMillis = retentionDays * 24 * 60 * 60 * 1000L
                val now = System.currentTimeMillis()

                val trashDir = getTrashDirectory(context)
                val files = trashDir.listFiles() ?: return@Thread

                for (file in files) {
                    if (file.isDirectory) {
                        val name = file.name
                        val suffixIndex = name.lastIndexOf(DELETED_SUFFIX)
                        if (suffixIndex != -1) {
                            try {
                                val deletedTime = name.substring(suffixIndex + DELETED_SUFFIX.length).toLong()
                                if (now - deletedTime > retentionMillis) {
                                    StorageOperations.deleteDir(file)
                                    Log.i(TAG, "Permanently deleted expired trash project: $name")
                                }
                            } catch (e: Exception) {
                                StorageOperations.deleteDir(file)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning expired trash", e)
            }
        }.start()
    }

    @JvmStatic
    fun showDeleteProjectDialog(context: Context, projectDir: File, onFinished: Runnable) {
        val density = context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (20 * density).toInt()
            setPadding(padding, padding, padding, 0)
        }

        val deletePermanentlyCheckBox = CheckBox(context).apply {
            text = context.getString(R.string.delete_permanently)
            isChecked = false
        }

        container.addView(deletePermanentlyCheckBox)

        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.delete_project_title, projectDir.name))
            .setMessage(context.getString(R.string.delete_project_message, projectDir.name))
            .setView(container)
            .setPositiveButton(R.string.delete) { dialog, _ ->
                if (deletePermanentlyCheckBox.isChecked) {
                    Thread {
                        try {
                            StorageOperations.deleteDir(projectDir)
                            Handler(Looper.getMainLooper()).post {
                                onFinished.run()
                            }
                        } catch (e: IOException) {
                            Log.e(TAG, "Failed to delete project permanently", e)
                        }
                    }.start()
                } else {
                    val success = moveToTrash(context, projectDir)
                    if (success) {
                        Handler(Looper.getMainLooper()).post {
                            onFinished.run()
                        }
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    @JvmStatic
    fun showTrashBinDialog(context: Context, onFinished: Runnable) {
        val trashDir = getTrashDirectory(context)
        val trashedFiles = trashDir.listFiles()?.filter { it.isDirectory && it.name.contains(DELETED_SUFFIX) } ?: emptyList()

        if (trashedFiles.isEmpty()) {
            AlertDialog.Builder(context)
                .setTitle(R.string.trash_bin_title)
                .setMessage(R.string.trash_bin_empty)
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val displayNames = trashedFiles.map { file ->
            val name = file.name
            val suffixIndex = name.lastIndexOf(DELETED_SUFFIX)
            if (suffixIndex != -1) name.substring(0, suffixIndex) else name
        }.toTypedArray()

        AlertDialog.Builder(context)
            .setTitle(R.string.trash_bin_title)
            .setItems(displayNames) { _, which ->
                val selectedFile = trashedFiles[which]
                val originalName = displayNames[which]

                AlertDialog.Builder(context)
                    .setTitle(originalName)
                    .setMessage(R.string.trash_bin_item_action)
                    .setPositiveButton(R.string.trash_bin_restore) { dialog, _ ->
                        restoreFromTrash(context, selectedFile)
                        Handler(Looper.getMainLooper()).post {
                            onFinished.run()
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.delete) { dialog, _ ->
                        Thread {
                            try {
                                StorageOperations.deleteDir(selectedFile)
                                Handler(Looper.getMainLooper()).post {
                                    onFinished.run()
                                }
                            } catch (e: IOException) {
                                Log.e(TAG, "Failed to delete trashed file", e)
                            }
                        }.start()
                        dialog.dismiss()
                    }
                    .setNeutralButton(R.string.cancel, null)
                    .show()
            }
            .setNeutralButton(R.string.trash_bin_empty_all) { dialog, _ ->
                AlertDialog.Builder(context)
                    .setTitle(R.string.trash_bin_empty_all_confirm_title)
                    .setMessage(R.string.trash_bin_empty_all_confirm_message)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        Thread {
                            try {
                                trashedFiles.forEach { StorageOperations.deleteDir(it) }
                                Handler(Looper.getMainLooper()).post {
                                    onFinished.run()
                                }
                            } catch (e: IOException) {
                                Log.e(TAG, "Failed to empty trash", e)
                            }
                        }.start()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                dialog.dismiss()
            }
            .setPositiveButton(R.string.close, null)
            .show()
    }

    @JvmStatic
    fun showDeleteMultipleProjectsDialog(
        context: Context,
        projectDirs: List<File>,
        onFinished: Runnable
    ) {
        val density = context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (20 * density).toInt()
            setPadding(padding, padding, padding, 0)
        }

        val deletePermanentlyCheckBox = CheckBox(context).apply {
            text = context.getString(R.string.delete_permanently)
            isChecked = false
        }

        container.addView(deletePermanentlyCheckBox)

        val size = projectDirs.size

        AlertDialog.Builder(context)
            .setTitle(context.resources.getQuantityString(R.plurals.delete_projects, size, size))
            .setMessage(context.getString(R.string.delete_project_multiple_message, size))
            .setView(container)
            .setPositiveButton(R.string.delete) { dialog, _ ->
                Thread {
                    try {
                        for (projectDir in projectDirs) {
                            if (deletePermanentlyCheckBox.isChecked) {
                                StorageOperations.deleteDir(projectDir)
                            } else {
                                moveToTrash(context, projectDir)
                            }
                        }
                        Handler(Looper.getMainLooper()).post {
                            onFinished.run()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to delete multiple projects", e)
                    }
                }.start()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
