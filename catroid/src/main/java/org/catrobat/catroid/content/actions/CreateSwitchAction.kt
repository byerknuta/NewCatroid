package org.catrobat.catroid.content.actions

import android.graphics.Color
import android.widget.FrameLayout
import androidx.appcompat.widget.SwitchCompat
import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class CreateSwitchAction : Action() {
    var scope: Scope? = null
    var viewId: Formula? = null
    var text: Formula? = null
    var defaultState: Formula? = null
    var x: Formula? = null
    var y: Formula? = null
    var width: Formula? = null
    var height: Formula? = null

    private var started = false
    @Volatile private var finished = false

    private fun Formula?.interpretSafeBoolean(scope: Scope?): Boolean {
        if (this == null) return false
        try {
            val obj = this.interpretObject(scope)
            if (obj is Boolean) return obj
            if (obj is Number) return obj.toDouble() != 0.0
            val str = obj?.toString()?.trim()?.lowercase() ?: ""
            return str == "true" || str == "истина" || str == "да" || str == "yes" || str == "1"
        } catch (e: Exception) {
            return false
        }
    }

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
        val labelText = text?.interpretString(scope) ?: ""
        val isChecked = defaultState.interpretSafeBoolean(scope)
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
                val switchView = SwitchCompat(stage)
                switchView.text = labelText
                switchView.isChecked = isChecked
                switchView.setTextColor(Color.WHITE)

                switchView.visibility = org.catrobat.catroid.common.NativeViewBindingManager.defaultVisibility

                val params = FrameLayout.LayoutParams(w, h).apply {
                    leftMargin = px
                    topMargin = py
                }
                stage.addViewToStage(id, switchView, params)
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
