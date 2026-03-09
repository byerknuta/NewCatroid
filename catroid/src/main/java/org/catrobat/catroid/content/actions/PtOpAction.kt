package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtOpAction() : TemporalAction() {
    var scope: Scope? = null
    var resFormula: Formula? = null
    var aFormula: Formula? = null
    var bFormula: Formula? = null
    var opType: String = "add"

    override fun update(percent: Float) {
        val res = resFormula?.interpretString(scope) ?: "res"
        val a = aFormula?.interpretString(scope) ?: "a"
        val b = bFormula?.interpretString(scope) ?: ""

        MLBridge.nativeOp(res, a, b, opType)
    }
}