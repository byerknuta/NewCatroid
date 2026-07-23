package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtLstmCellAction : TemporalAction() {
    var scope: Scope? = null
    var layerNameFormula: Formula? = null
    var inputFormula: Formula? = null
    var hInFormula: Formula? = null
    var cInFormula: Formula? = null
    var hOutFormula: Formula? = null
    var cOutFormula: Formula? = null
    var inDimFormula: Formula? = null
    var hiddenDimFormula: Formula? = null

    override fun update(percent: Float) {
        val layerName = layerNameFormula?.interpretString(scope) ?: "lstm1"
        val input = inputFormula?.interpretString(scope) ?: "x"
        val hIn = hInFormula?.interpretString(scope) ?: "hin"
        val cIn = cInFormula?.interpretString(scope) ?: "cin"
        val hOut = hOutFormula?.interpretString(scope) ?: "hout"
        val cOut = cOutFormula?.interpretString(scope) ?: "cout"
        val inDim = inDimFormula?.interpretInteger(scope) ?: 8
        val hiddenDim = hiddenDimFormula?.interpretInteger(scope) ?: 16

        MLBridge.nativeLayerLstmCell(layerName, input, hIn, cIn, hOut, cOut, inDim, hiddenDim)
    }
}
