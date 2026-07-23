package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtClipGradAction : TemporalAction() {
    var scope: Scope? = null
    var maxNormFormula: Formula? = null

    override fun update(percent: Float) {
        val maxNorm = maxNormFormula?.interpretFloat(scope) ?: 5.0f
        MLBridge.nativeClipGrad(maxNorm)
    }
}
