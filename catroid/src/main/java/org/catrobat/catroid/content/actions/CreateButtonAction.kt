package org.catrobat.catroid.content.actions

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.Button
import android.widget.FrameLayout
import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class CreateButtonAction : Action() {
    var scope: Scope? = null
    var viewId: Formula? = null
    var text: Formula? = null
    var colorHex: Formula? = null
    var fontSize: Formula? = null
    var bgColor: Formula? = null
    var cornerRadius: Formula? = null
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
        val rawText = text?.interpretString(scope) ?: ""
        val cHex = colorHex?.interpretString(scope) ?: "#FFFFFF"
        val size = fontSize?.interpretFloat(scope) ?: 18f
        val bgHex = bgColor?.interpretString(scope) ?: "#2196F3"
        val radius = cornerRadius?.interpretInteger(scope) ?: 8
        val px = x?.interpretInteger(scope) ?: 0
        val py = y?.interpretInteger(scope) ?: 0
        val w = width?.interpretInteger(scope) ?: 300
        val h = height?.interpretInteger(scope) ?: 100

        if (id.isEmpty()) {
            finished = true
            return
        }

        stage.runOnUiThread {
            try {
                val button = Button(stage)
                button.text = rawText
                button.textSize = size

                try {
                    button.setTextColor(Color.parseColor(cHex))
                } catch (e: Exception) {
                    button.setTextColor(Color.WHITE)
                }

                val backgroundShape = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = radius.toFloat()
                    try {
                        setColor(Color.parseColor(bgHex))
                    } catch (e: Exception) {
                        setColor(Color.BLUE)
                    }
                }
                button.background = backgroundShape

                button.visibility = org.catrobat.catroid.common.NativeViewBindingManager.defaultVisibility

                val params = FrameLayout.LayoutParams(w, h).apply {
                    leftMargin = px
                    topMargin = py
                }
                stage.addViewToStage(id, button, params)
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
