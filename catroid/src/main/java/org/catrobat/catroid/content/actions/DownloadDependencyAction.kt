package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.CatroidApplication
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.formulaeditor.UserVariable
import org.catrobat.catroid.ide.DependencyManager
import java.io.File
import kotlin.concurrent.thread

class DownloadDependencyAction : Action() {
    var scope: Scope? = null
    var libraryId: Formula? = null
    var repositories: Formula? = null
    var recursive: Boolean = true
    private var resultVariable: UserVariable? = null

    private var started = false
    @Volatile private var finished = false

    override fun act(delta: Float): Boolean {
        if (!started) {
            started = true
            runAsyncDownload()
        }
        return finished
    }

    private fun runAsyncDownload() {
        thread(start = true) {
            val context = CatroidApplication.getAppContext()
            try {
                val projectDir = scope?.project?.getFilesDir() ?: return@thread
                val libIdStr = libraryId?.interpretString(scope) ?: ""
                val reposStr = repositories?.interpretString(scope) ?: ""

                if (libIdStr.trim().isEmpty()) {
                    finished = true
                    return@thread
                }

                val customRepos = reposStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                val success = DependencyManager.downloadLibraryRecursive(
                    context = context,
                    projectPath = projectDir.absolutePath,
                    libString = libIdStr,
                    customRepos = customRepos,
                    recursive = recursive,
                    onProgressUpdate = { _, _ -> }
                )

                if (success) {
                    val libsDir = File(projectDir, "libs")
                    val jarNames = libsDir.listFiles()?.filter { it.isFile && it.extension == "jar" }?.map { it.name } ?: emptyList()
                    val resultListStr = jarNames.joinToString(",")

                    com.badlogic.gdx.Gdx.app.postRunnable {
                        resultVariable?.value = resultListStr
                    }
                } else {
                    com.badlogic.gdx.Gdx.app.postRunnable {
                        resultVariable?.value = "ERROR_DOWNLOAD_FAILED"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                com.badlogic.gdx.Gdx.app.postRunnable {
                    resultVariable?.value = "ERROR: ${e.localizedMessage}"
                }
            } finally {
                finished = true
            }
        }
    }

    fun setResultVariable(variable: UserVariable?) {
        this.resultVariable = variable
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
        libraryId = null
        repositories = null
        resultVariable = null
        recursive = true
    }
}
