package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class SetRenderResolutionAction : TemporalAction() {
    var scope: Scope? = null
    var scale: Formula? = null
    var aspectMode: Formula? = null

    override fun update(percent: Float) {
        val scaleValue = scale?.interpretFloat(scope) ?: 1.0f
        val modeValue = aspectMode?.interpretInteger(scope) ?: 0

        StageActivity.getActiveStageListener()?.threeDManager?.setRenderResolution(scaleValue, modeValue)
    }
}
