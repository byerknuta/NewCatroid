package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.concurrent.thread

class ZipProjectFilesAction : Action() {
    var scope: Scope? = null
    var zipFileName: Formula? = null
    var filesToZip: Formula? = null

    private var started = false
    @Volatile private var finished = false

    override fun act(delta: Float): Boolean {
        if (!started) {
            started = true
            runAsyncZip()
        }
        return finished
    }

    private fun runAsyncZip() {
        thread(start = true) {
            try {
                val projectDir = scope?.project?.getFilesDir() ?: return@thread
                val zipName = zipFileName?.interpretString(scope) ?: "archive.zip"
                val zipFile = scope?.project?.getFile(zipName) ?: return@thread

                val filesStr = filesToZip?.interpretString(scope) ?: ""
                val fileNames = filesStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                zipFiles(projectDir, fileNames, zipFile)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                finished = true
            }
        }
    }

    private fun zipFiles(projectDir: File, fileNames: List<String>, zipFile: File) {
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            for (fileName in fileNames) {
                val fileToZip = File(projectDir, fileName)
                if (!fileToZip.exists()) continue
                if (fileToZip.isDirectory) {
                    zipDirectory(fileToZip, fileToZip.name, zos)
                } else {
                    zipSingleFile(fileToZip, "", zos)
                }
            }
        }
    }

    private fun zipDirectory(dir: File, basePath: String, zos: ZipOutputStream) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            val path = if (basePath.isEmpty()) file.name else "$basePath/${file.name}"
            if (file.isDirectory) {
                zipDirectory(file, path, zos)
            } else {
                zipSingleFile(file, basePath, zos)
            }
        }
    }

    private fun zipSingleFile(file: File, basePath: String, zos: ZipOutputStream) {
        val entryName = if (basePath.isEmpty()) file.name else "$basePath/${file.name}"
        FileInputStream(file).use { fis ->
            zos.putNextEntry(ZipEntry(entryName))
            fis.copyTo(zos)
            zos.closeEntry()
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
        zipFileName = null
        filesToZip = null
    }
}
