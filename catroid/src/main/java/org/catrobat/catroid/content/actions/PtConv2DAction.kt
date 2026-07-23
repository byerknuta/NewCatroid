package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtConv2DAction : TemporalAction() {
    var scope: Scope? = null
    var layerNameFormula: Formula? = null
    var inputFormula: Formula? = null
    var outputFormula: Formula? = null
    var inChannelsFormula: Formula? = null
    var outChannelsFormula: Formula? = null
    var kernelSizeFormula: Formula? = null
    var strideFormula: Formula? = null

    override fun update(percent: Float) {
        val layerName = layerNameFormula?.interpretString(scope) ?: "conv1"
        val input = inputFormula?.interpretString(scope) ?: "img"
        val output = outputFormula?.interpretString(scope) ?: "features"
        val inC = inChannelsFormula?.interpretInteger(scope) ?: 1
        val outC = outChannelsFormula?.interpretInteger(scope) ?: 16
        val kSize = kernelSizeFormula?.interpretInteger(scope) ?: 3
        val stride = strideFormula?.interpretInteger(scope) ?: 1

        MLBridge.nativeLayerConv2D(layerName, input, output, inC, outC, kSize, stride)
    }
}
