package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtGruCellAction : TemporalAction() {
    var scope: Scope? = null
    var layerNameFormula: Formula? = null
    var inputFormula: Formula? = null
    var hInFormula: Formula? = null
    var hOutFormula: Formula? = null
    var inDimFormula: Formula? = null
    var hiddenDimFormula: Formula? = null

    override fun update(percent: Float) {
        val layerName = layerNameFormula?.interpretString(scope) ?: "gru1"
        val input = inputFormula?.interpretString(scope) ?: "x"
        val hIn = hInFormula?.interpretString(scope) ?: "hin"
        val hOut = hOutFormula?.interpretString(scope) ?: "hout"
        val inDim = inDimFormula?.interpretInteger(scope) ?: 8
        val hiddenDim = hiddenDimFormula?.interpretInteger(scope) ?: 16

        MLBridge.nativeLayerGruCell(layerName, input, hIn, hOut, inDim, hiddenDim)
    }
}
