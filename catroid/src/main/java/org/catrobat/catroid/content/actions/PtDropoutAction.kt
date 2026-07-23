package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtDropoutAction : TemporalAction() {
    var scope: Scope? = null
    var resFormula: Formula? = null
    var inputFormula: Formula? = null
    var probFormula: Formula? = null

    override fun update(percent: Float) {
        val res = resFormula?.interpretString(scope) ?: "drop_out"
        val input = inputFormula?.interpretString(scope) ?: "in"
        val p = probFormula?.interpretFloat(scope) ?: 0.2f

        MLBridge.nativeDropout(res, input, p)
    }
}
