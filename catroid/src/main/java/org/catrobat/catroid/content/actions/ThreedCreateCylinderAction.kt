package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class ThreedCreateCylinderAction : TemporalAction() {
    var scope: Scope? = null
    var objectId: Formula? = null

    override fun update(percent: Float) {
        val listener = StageActivity.getActiveStageListener() ?: return
        val threeDManager = listener.threeDManager ?: return

        try {
            val id = objectId!!.interpretString(scope)
            if (!id.isEmpty()) {
                threeDManager.createCylinder(id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}