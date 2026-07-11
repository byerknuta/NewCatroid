package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope

class StopSpritesheetAction : TemporalAction() {
    var scope: Scope? = null

    override fun update(percent: Float) {
        val sprite = scope?.sprite ?: return
        sprite.look?.stopSpritesheet()
    }

    override fun reset() {
        super.reset()
        scope = null
    }
}
