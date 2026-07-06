package org.catrobat.catroid.content.actions

import android.graphics.BitmapFactory
import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class CreateImageViewAction : Action() {
    var scope: Scope? = null
    var viewId: Formula? = null
    var source: Formula? = null
    var cornerRadius: Formula? = null
    var scaleSelection: Int = 0
    var x: Formula? = null
    var y: Formula? = null
    var width: Formula? = null
    var height: Formula? = null

    private var started = false
    @Volatile private var finished = false

    override fun act(delta: Float): Boolean {
        if (!started) {
            started = true
            runAsyncCreate()
        }
        return finished
    }

    private fun runAsyncCreate() {
        val stage = StageActivity.activeStageActivity?.get()
        if (stage == null) {
            finished = true
            return
        }

        val id = viewId?.interpretString(scope) ?: ""
        val srcStr = source?.interpretString(scope) ?: ""
        val radius = cornerRadius?.interpretInteger(scope) ?: 0
        val px = x?.interpretInteger(scope) ?: 0
        val py = y?.interpretInteger(scope) ?: 0
        val w = width?.interpretInteger(scope) ?: 300
        val h = height?.interpretInteger(scope) ?: 300

        if (id.isEmpty()) {
            finished = true
            return
        }

        stage.runOnUiThread {
            try {
                val imageView = ImageView(stage)

                imageView.scaleType = when (scaleSelection) {
                    1 -> ImageView.ScaleType.CENTER_CROP
                    2 -> ImageView.ScaleType.FIT_XY
                    3 -> ImageView.ScaleType.CENTER
                    else -> ImageView.ScaleType.FIT_CENTER
                }

                if (radius > 0) {
                    imageView.outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: View, outline: Outline) {
                            outline.setRoundRect(0, 0, view.width, view.height, radius.toFloat())
                        }
                    }
                    imageView.clipToOutline = true
                }

                if (srcStr.startsWith("http://") || srcStr.startsWith("https://")) {
                    thread(start = true) {
                        try {
                            val connection = URL(srcStr).openConnection() as HttpURLConnection
                            connection.doInput = true
                            connection.connect()
                            val bitmap = BitmapFactory.decodeStream(connection.inputStream)
                            stage.runOnUiThread {
                                imageView.setImageBitmap(bitmap)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    val file = scope?.project?.getFile(srcStr)
                    if (file != null && file.exists()) {
                        try {
                            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                            imageView.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                imageView.visibility = org.catrobat.catroid.common.NativeViewBindingManager.defaultVisibility

                val params = FrameLayout.LayoutParams(w, h).apply {
                    leftMargin = px
                    topMargin = py
                }
                stage.addViewToStage(id, imageView, params)
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
    }
}
