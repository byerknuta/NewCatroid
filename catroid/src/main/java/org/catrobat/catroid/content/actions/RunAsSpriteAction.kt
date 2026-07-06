package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.content.Script
import org.catrobat.catroid.content.Sprite
import org.catrobat.catroid.content.bricks.Brick
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class RunAsSpriteAction(
    private val script: Script,
    private val nestedBricks: List<Brick>
) : Action() {

    var scope: Scope? = null
    var spriteName: Formula? = null

    override fun act(delta: Float): Boolean {
        val targetName = spriteName?.interpretString(scope) ?: return true
        val stageListener = StageActivity.getActiveStageListener() ?: return true

        val targetSprites = stageListener.spritesFromStage.filter { sprite ->
            sprite.name == targetName
        }

        for (targetSprite in targetSprites) {
            val dummyScript = org.catrobat.catroid.content.StartScript()
            val targetSequence = targetSprite.createSequenceAction(dummyScript)

            for (brick in nestedBricks) {
                if (!brick.isCommentedOut) {
                    brick.addActionToSequence(targetSprite, targetSequence)
                }
            }

            targetSprite.look.addAction(targetSequence)
        }

        return true
    }
}
