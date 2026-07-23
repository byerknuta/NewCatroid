package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtAttentionAction : TemporalAction() {
    var scope: Scope? = null
    var layerNameFormula: Formula? = null
    var inputFormula: Formula? = null
    var outputFormula: Formula? = null
    var embedDimFormula: Formula? = null

    override fun update(percent: Float) {
        val layerName = layerNameFormula?.interpretString(scope) ?: "attn1"
        val input = inputFormula?.interpretString(scope) ?: "in"
        val output = outputFormula?.interpretString(scope) ?: "out"
        val dim = embedDimFormula?.interpretInteger(scope) ?: 8

        MLBridge.nativeLayerAttention(layerName, input, output, dim)
    }
}
