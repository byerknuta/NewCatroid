package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtSliceAction : TemporalAction() {
    var scope: Scope? = null
    var resFormula: Formula? = null
    var inputFormula: Formula? = null
    var startColFormula: Formula? = null
    var endColFormula: Formula? = null

    override fun update(percent: Float) {
        val res = resFormula?.interpretString(scope) ?: "res"
        val input = inputFormula?.interpretString(scope) ?: "in"
        val start = startColFormula?.interpretInteger(scope) ?: 0
        val end = endColFormula?.interpretInteger(scope) ?: 1

        MLBridge.nativeSlice(res, input, start, end)
    }
}
