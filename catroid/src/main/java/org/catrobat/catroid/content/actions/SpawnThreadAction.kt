package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.content.Script
import org.catrobat.catroid.content.bricks.Brick
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.utils.ActionThreadRegistry

class SpawnThreadAction : Action() {
    var scope: Scope? = null
    var threadIdFormula: Formula? = null
    var threadBricks: List<Brick>? = null
    var script: Script? = null

    override fun act(delta: Float): Boolean {
        val currentActor = actor ?: return true
        val currentScope = scope ?: return true
        val bricks = threadBricks ?: return true
        val currentScript = script ?: return true
        val rawId = threadIdFormula?.interpretString(currentScope) ?: "default_thread"

        val threadSequence = ScriptSequenceAction(currentScript)
        for (brick in bricks) {
            if (!brick.isCommentedOut) {
                brick.addActionToSequence(currentScope.sprite, threadSequence)
            }
        }

        val trackedAction = TrackedThreadAction(rawId, threadSequence)

        ActionThreadRegistry.register(rawId, trackedAction)

        currentActor.addAction(trackedAction)
        return true
    }
}
