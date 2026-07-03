package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import org.catrobat.catroid.utils.ActionThreadRegistry

class TrackedThreadAction(
    private val threadId: String,
    private val innerAction: Action
) : Action() {

    override fun act(delta: Float): Boolean {
        val finished = innerAction.act(delta)
        if (finished) {
            ActionThreadRegistry.unregister(threadId, this)
        }
        return finished
    }

    override fun setActor(actor: Actor?) {
        super.setActor(actor)
        innerAction.actor = actor
    }

    override fun restart() {
        innerAction.restart()
        super.restart()
    }
}
