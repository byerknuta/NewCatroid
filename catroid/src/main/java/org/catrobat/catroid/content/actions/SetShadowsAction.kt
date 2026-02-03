package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class SetShadowsAction : TemporalAction() {
    var scope: Scope? = null
    var enabledFormula: Formula? = null

    override fun update(percent: Float) {
        val enabled = enabledFormula?.interpretBoolean(scope) ?: true
        StageActivity.getActiveStageListener().threeDManager.setShadowsEnabled(enabled)
    }
}