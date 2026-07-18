package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class Replace3DModelAction() : TemporalAction() {
    var scope: Scope? = null
    var objectId: Formula? = null
    var modelPath: Formula? = null

    override fun update(percent: Float) {
        val idStr = objectId?.interpretString(scope) ?: ""
        val pathStr = modelPath?.interpretString(scope) ?: ""

        StageActivity.getActiveStageListener()?.threeDManager?.replaceModel(idStr, pathStr)
    }
}
