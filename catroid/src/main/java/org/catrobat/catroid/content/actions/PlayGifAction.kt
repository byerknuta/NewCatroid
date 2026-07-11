package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula

class PlayGifAction : TemporalAction() {
    var scope: Scope? = null
    var gifFileName: Formula? = null

    override fun update(percent: Float) {
        val sprite = scope?.sprite ?: return
        val look = sprite.look ?: return
        val nameStr = gifFileName?.interpretString(scope) ?: ""

        if (nameStr.isNotEmpty()) {
            look.playGif(nameStr)
        }
    }

    override fun reset() {
        super.reset()
        scope = null
        gifFileName = null
    }
}
