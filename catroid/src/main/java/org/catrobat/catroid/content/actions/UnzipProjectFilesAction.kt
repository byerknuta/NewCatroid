package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream
import kotlin.concurrent.thread

class UnzipProjectFilesAction : Action() {
    var scope: Scope? = null
    var zipFileName: Formula? = null

    private var started = false
    @Volatile private var finished = false

    override fun act(delta: Float): Boolean {
        if (!started) {
            started = true
            runAsyncUnzip()
        }
        return finished
    }

    private fun runAsyncUnzip() {
        thread(start = true) {
            try {
                val projectDir = scope?.project?.getFilesDir() ?: return@thread
                val zipName = zipFileName?.interpretString(scope) ?: "archive.zip"
                val zipFile = scope?.project?.getFile(zipName) ?: return@thread

                if (zipFile.exists()) {
                    unzipFile(zipFile, projectDir)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                finished = true
            }
        }
    }

    private fun unzipFile(zipFile: File, targetDir: File) {
        ZipInputStream(zipFile.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val newFile = File(targetDir, entry.name)

                if (!newFile.canonicalPath.startsWith(targetDir.canonicalPath)) {
                    entry = zis.nextEntry
                    continue
                }

                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
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
        zipFileName = null
    }
}
