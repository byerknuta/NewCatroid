package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class Fast2DSetGravityAction : TemporalAction() {
    var scope: Scope? = null
    var gravityX: Formula? = null
    var gravityY: Formula? = null

    override fun update(percent: Float) {
        val gx = gravityX?.interpretFloat(scope) ?: 0f
        val gy = gravityY?.interpretFloat(scope) ?: -9.8f

        val stageListener = StageActivity.activeStageActivity.get()?.stageListener
        stageListener?.fastTwoDManager?.setGravity(gx, gy)
    }
}
