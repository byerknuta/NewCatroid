package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtSetTensorAction : TemporalAction() {
    var scope: Scope? = null
    var nameFormula: Formula? = null
    var dataFormula: Formula? = null

    override fun update(percent: Float) {
        val name = nameFormula?.interpretString(scope) ?: "t1"
        val data = dataFormula?.interpretString(scope) ?: ""
        MLBridge.nativeSetTensor(name, data)
    }
}