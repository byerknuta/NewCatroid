package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class CreateWebUrlAction : Action() {
    var scope: Scope? = null
    var url: Formula? = null
    var name: Formula? = null
    var posX: Formula? = null
    var posY: Formula? = null
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
        val activity = StageActivity.activeStageActivity?.get()
        if (activity == null) {
            finished = true
            return
        }

        val urlv = url?.interpretString(scope) ?: ""
        val namev = name?.interpretString(scope) ?: ""
        val posXv = posX?.interpretInteger(scope) ?: 0
        val posYv = posY?.interpretInteger(scope) ?: 0
        val widthv = width?.interpretInteger(scope) ?: 0
        val heightv = height?.interpretInteger(scope) ?: 0

        if (namev.isEmpty()) {
            finished = true
            return
        }

        activity.runOnUiThread {
            try {
                activity.createWebViewWithUrl(namev, urlv, posXv, posYv, widthv, heightv)
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
