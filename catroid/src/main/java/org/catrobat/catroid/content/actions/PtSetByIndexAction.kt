package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtSetByIndexAction : TemporalAction() {
    var scope: Scope? = null
    var nameFormula: Formula? = null
    var indexFormula: Formula? = null
    var valueFormula: Formula? = null

    override fun update(percent: Float) {
        val name = nameFormula?.interpretString(scope) ?: "t1"
        val index = indexFormula?.interpretInteger(scope) ?: 0
        val value = valueFormula?.interpretFloat(scope) ?: 0.0f
        MLBridge.nativeSetTensorByIndex(name, index, value)
    }
}