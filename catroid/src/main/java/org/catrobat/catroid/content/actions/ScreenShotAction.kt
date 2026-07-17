package org.catrobat.catroid.content.actions

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import com.badlogic.gdx.utils.ScreenUtils
import org.catrobat.catroid.ProjectManager
import org.catrobat.catroid.common.LookData
import org.catrobat.catroid.content.MyActivityManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.io.StorageOperations
import org.catrobat.catroid.stage.ScreenshotSaver
import org.catrobat.catroid.stage.ScreenshotSaverCallback
import org.catrobat.catroid.stage.StageActivity
import java.io.File
import java.io.InputStream

@Suppress("DEPRECATION")
open class ScreenShotAction : TemporalAction() {
    var scope: Scope? = null
    var name: String? = null

    private var isDone = false
    private var started = false

    companion object {
        private const val TAG = "ScreenShotAction"
        private const val DEFAULT_FILENAME = "screenshot"
        private const val FILE_EXTENSION = ".png"
    }

    override fun act(delta: Float): Boolean {
        if (isDone) return true

        if (!started) {
            started = true
            captureAndSetLookAsync()
        }

        return false
    }

    override fun update(percent: Float) {
    }

    override fun restart() {
        super.restart()
        isDone = false
        started = false
    }

    override fun reset() {
        super.reset()
        isDone = false
        started = false
    }

    private fun captureAndSetLookAsync() {
        val activity = StageActivity.activeStageActivity.get() ?: run {
            Log.e(TAG, "StageActivity is null, cannot take screenshot.")
            isDone = true
            return
        }

        val filesDir = getScreenshotPath()

        activity.runOnUiThread {
            activity.captureScreenWithNativeViews(filesDir, "$DEFAULT_FILENAME$FILE_EXTENSION", object : ScreenshotSaverCallback {
                override fun screenshotSaved(success: Boolean) {
                    if (success) {
                        val savedFile = File(filesDir, "$DEFAULT_FILENAME$FILE_EXTENSION")
                        Thread {
                            try {
                                val finalFileName = name ?: DEFAULT_FILENAME
                                val tempFile = File.createTempFile(finalFileName, FILE_EXTENSION)
                                tempFile.deleteOnExit()

                                savedFile.inputStream().use { input ->
                                    tempFile.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                savedFile.delete()

                                val lookData = LookData(finalFileName, tempFile).apply {
                                    collisionInformation.calculate()
                                }

                                Gdx.app.postRunnable {
                                    setLook(lookData)
                                    isDone = true
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to build LookData from file", e)
                                Gdx.app.postRunnable { isDone = true }
                            }
                        }.start()
                    } else {
                        Log.e(TAG, "Failed to capture hybrid screenshot")
                        Gdx.app.postRunnable { isDone = true }
                    }
                }
            })
        }
    }

    private fun getScreenshotPath(): String {
        val scene = ProjectManager.getInstance().currentlyPlayingScene
        return scene.directory.absolutePath + "/"
    }

    private fun setLook(lookData: LookData) {
        val sprite = scope?.sprite ?: return

        lookData.apply {
            updateLookListIndex()
            sprite.look.lookData = this
            collisionInformation?.collisionPolygonCalculationThread?.join()
            isWebRequest = true
        }
    }

    private fun updateLookListIndex() {
        val currentLook = scope?.sprite?.look
        if (currentLook != null && currentLook.lookListIndexBeforeLookRequest <= -1) {
            val lookList = scope?.sprite?.lookList
            currentLook.lookListIndexBeforeLookRequest = lookList?.indexOf(currentLook.lookData) ?: -1
        }
    }
}
