package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtLayerLinearAction : TemporalAction() {
    var scope: Scope? = null
    var layerNameFormula: Formula? = null
    var inputFormula: Formula? = null
    var outputFormula: Formula? = null
    var inFeaturesFormula: Formula? = null
    var outFeaturesFormula: Formula? = null

    override fun update(percent: Float) {
        val layerName = layerNameFormula?.interpretString(scope) ?: "fc1"
        val input = inputFormula?.interpretString(scope) ?: "in"
        val output = outputFormula?.interpretString(scope) ?: "out"
        val inF = inFeaturesFormula?.interpretInteger(scope) ?: 10
        val outF = outFeaturesFormula?.interpretInteger(scope) ?: 5

        MLBridge.nativeLayerLinear(layerName, input, output, inF, outF)
    }
}
