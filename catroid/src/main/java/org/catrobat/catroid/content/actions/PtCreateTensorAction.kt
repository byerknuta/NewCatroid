package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtCreateTensorAction() : TemporalAction() {
    var scope: Scope? = null
    var nameFormula: Formula? = null
    var shapeFormula: Formula? = null
    var valueFormula: Formula? = null
    var trainableFormula: Formula? = null

    override fun update(percent: Float) {
        val name = nameFormula?.interpretString(scope) ?: "t1"
        val shapeRaw = shapeFormula?.interpretString(scope) ?: "1"
        if (valueFormula?.interpretString(scope) == "RANDOM") {
            val isTrainable = (trainableFormula?.interpretFloat(scope) ?: 0.0f) > 0.5f

            val shape = shapeRaw.split(",")
                .map { it.trim().toInt() }
                .toIntArray()

            MLBridge.nativeCreateRandomTensor(name, shape, isTrainable)
        } else {
            val value = valueFormula?.interpretFloat(scope) ?: 0.0f
            val isTrainable = (trainableFormula?.interpretFloat(scope) ?: 0.0f) > 0.5f

            val shape = shapeRaw.split(",")
                .map { it.trim().toInt() }
                .toIntArray()

            MLBridge.nativeCreateTensor(name, shape, value, isTrainable)
        }
    }
}