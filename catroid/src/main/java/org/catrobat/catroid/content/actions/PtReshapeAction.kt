package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtReshapeAction : TemporalAction() {
    var scope: Scope? = null
    var nameFormula: Formula? = null
    var shapeFormula: Formula? = null

    override fun update(percent: Float) {
        val name = nameFormula?.interpretString(scope) ?: ""
        val shapeStr = shapeFormula?.interpretString(scope) ?: ""

        // Превращаем "28, 28" в [28, 28]
        val shapeIntArray = shapeStr.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toIntArray()

        if (name.isNotEmpty() && shapeIntArray.isNotEmpty()) {
            MLBridge.nativeReshape(name, shapeIntArray)
        }
    }
}