package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class SetSpawnInvisibleAction : TemporalAction() {
    var scope: Scope? = null
    var objectId: Formula? = null

    override fun update(percent: Float) {
        val threeDManager = StageActivity.getActiveStageListener()?.threeDManager ?: return
        val id = objectId?.interpretString(scope) ?: return

        if (id.isNotEmpty()) {
            threeDManager.flagObjectForHiddenSpawn(id)
        }
    }
}