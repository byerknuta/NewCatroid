package org.catrobat.catroid.content.actions

import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.common.NativeViewBindingManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class CreateSliderAction : Action() {
    var scope: Scope? = null
    var viewId: Formula? = null
    var minValue: Formula? = null
    var maxValue: Formula? = null
    var currentValue: Formula? = null
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
        val min = minValue?.interpretFloat(scope) ?: 0.0f
        val max = maxValue?.interpretFloat(scope) ?: 100.0f
        val current = currentValue?.interpretFloat(scope) ?: 0.0f
        val px = x?.interpretInteger(scope) ?: 0
        val py = y?.interpretInteger(scope) ?: 0
        val w = width?.interpretInteger(scope) ?: 300
        val h = height?.interpretInteger(scope) ?: 60

        if (id.isEmpty()) {
            finished = true
            return
        }

        stage.runOnUiThread {
            try {
                val seekBar = SeekBar(stage)

                seekBar.max = 1000
                val progress = (((current - min) / (max - min)) * 1000).toInt()
                seekBar.progress = Math.max(0, Math.min(1000, progress))

                seekBar.tag = Pair(min, max)

                seekBar.visibility = NativeViewBindingManager.defaultVisibility

                val params = FrameLayout.LayoutParams(w, h).apply {
                    leftMargin = px
                    topMargin = py
                }
                stage.addViewToStage(id, seekBar, params)
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
        viewId = null
        minValue = null
        maxValue = null
        currentValue = null
        x = null
        y = null
        width = null
        height = null
    }
}
