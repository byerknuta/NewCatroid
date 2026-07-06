package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class CreateVideoAction : Action() {
    var scope: Scope? = null
    var name: Formula? = null
    var file: Formula? = null
    var posX: Formula? = null
    var posY: Formula? = null
    var width: Formula? = null
    var height: Formula? = null
    var loop: Formula? = null
    var controls: Formula? = null

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

        val nameT = name?.interpretString(scope) ?: ""
        val fileT = file?.interpretString(scope) ?: ""
        val loopT = loop?.interpretBoolean(scope) ?: false
        val controlsT = controls?.interpretBoolean(scope) ?: false
        val posXT = posX?.interpretInteger(scope) ?: 0
        val posYT = posY?.interpretInteger(scope) ?: 0
        val widthT = width?.interpretInteger(scope) ?: 0
        val heightT = height?.interpretInteger(scope) ?: 0

        val projFile = scope?.project?.getFile(fileT)
        if (nameT.isEmpty() || projFile == null) {
            finished = true
            return
        }

        activity.runOnUiThread {
            try {
                activity.createVideoPlayer(
                    nameT,
                    projFile.absolutePath,
                    posXT, posYT, widthT, heightT,
                    controlsT, loopT, true
                )
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
