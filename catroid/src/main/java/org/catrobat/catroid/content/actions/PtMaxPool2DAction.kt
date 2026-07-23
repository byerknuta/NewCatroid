package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtMaxPool2DAction : TemporalAction() {
    var scope: Scope? = null
    var resFormula: Formula? = null
    var inputFormula: Formula? = null
    var poolSizeFormula: Formula? = null
    var strideFormula: Formula? = null

    override fun update(percent: Float) {
        val res = resFormula?.interpretString(scope) ?: "pooled"
        val input = inputFormula?.interpretString(scope) ?: "features"
        val pSize = poolSizeFormula?.interpretInteger(scope) ?: 2
        val stride = strideFormula?.interpretInteger(scope) ?: 2

        MLBridge.nativeMaxPool2D(res, input, pSize, stride)
    }
}
