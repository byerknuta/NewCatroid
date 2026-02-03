package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class SetRotationLockAction : TemporalAction() {
    var scope: Scope? = null
    var objectId: Formula? = null


    var lockX: Boolean = false
    var lockY: Boolean = false
    var lockZ: Boolean = false

    override fun update(percent: Float) {
        val threeDManager = StageActivity.getActiveStageListener()?.threeDManager ?: return

        val id = objectId?.interpretString(scope) ?: return
        if (id.isEmpty()) return


        val xFactor = if (lockX) 0f else 1f
        val yFactor = if (lockY) 0f else 1f
        val zFactor = if (lockZ) 0f else 1f

        threeDManager.setAngularFactor(id, xFactor, yFactor, zFactor)
    }
}