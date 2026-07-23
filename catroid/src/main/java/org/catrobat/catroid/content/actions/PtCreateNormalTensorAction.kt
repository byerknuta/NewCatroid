package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtCreateNormalTensorAction : TemporalAction() {
    var scope: Scope? = null
    var nameFormula: Formula? = null
    var shapeFormula: Formula? = null
    var meanFormula: Formula? = null
    var stdFormula: Formula? = null
    var trainableFormula: Formula? = null

    override fun update(percent: Float) {
        val name = nameFormula?.interpretString(scope) ?: "noise"
        val shapeRaw = shapeFormula?.interpretString(scope) ?: "1,10"
        val mean = meanFormula?.interpretFloat(scope) ?: 0.0f
        val std = stdFormula?.interpretFloat(scope) ?: 1.0f
        val isTrainable = (trainableFormula?.interpretFloat(scope) ?: 0.0f) > 0.5f

        val shape = shapeRaw.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toIntArray()

        if (shape.isNotEmpty()) {
            MLBridge.nativeCreateNormalTensor(name, shape, mean, std, isTrainable)
        }
    }
}
