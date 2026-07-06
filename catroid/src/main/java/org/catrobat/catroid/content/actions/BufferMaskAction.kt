package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula

class BufferMaskAction : TemporalAction() {
    var scope: Scope? = null
    var bufferName: Formula? = null
    var modeSelection: Int = 0

    override fun update(percent: Float) {
        val sprite = scope?.sprite ?: return
        val nameStr = bufferName?.interpretString(scope) ?: ""

        if (nameStr.isNotEmpty() && nameStr.lowercase() != "none") {
            sprite.look.maskBufferName = nameStr
            sprite.look.maskMode = modeSelection
        } else {
            sprite.look.maskBufferName = null
        }
    }

    override fun restart() {
        super.restart()
    }

    override fun reset() {
        super.reset()
        scope = null
        bufferName = null
        modeSelection = 0
    }
}
