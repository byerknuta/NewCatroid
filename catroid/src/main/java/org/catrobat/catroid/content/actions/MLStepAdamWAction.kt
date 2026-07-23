package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class MLStepAdamWAction : TemporalAction() {
    var scope: Scope? = null
    var lrFormula: Formula? = null
    var weightDecayFormula: Formula? = null

    override fun update(percent: Float) {
        val lrVal = lrFormula?.interpretFloat(scope) ?: 0.001f
        val decay = weightDecayFormula?.interpretFloat(scope) ?: 0.01f
        MLBridge.nativeStepAdamW(lrVal, decay)
    }
}
