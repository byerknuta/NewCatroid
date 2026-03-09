package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtStepAction : TemporalAction() {
    var scope: Scope? = null
    var lrFormula: Formula? = null

    override fun update(percent: Float) {
        val lr = lrFormula?.interpretFloat(scope) ?: 0.01f
        MLBridge.nativeStep(lr)
    }
}