package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class Set3DSoundMaxDistanceAction : TemporalAction() {
    var scope: Scope? = null
    var instanceName: Formula? = null
    var distance: Formula? = null

    override fun update(percent: Float) {
        val name = instanceName?.interpretString(scope) ?: ""
        val dist = distance?.interpretFloat(scope) ?: 250f

        StageActivity.getActiveStageListener()?.threeDManager?.set3DSoundMaxDistance(name, dist)
    }
}
