package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class  PtSetTraining : TemporalAction() {
    var scope: Scope? = null
    var trainingFormula: Formula? = null

    override fun update(percent: Float) {
        val tg = trainingFormula?.interpretBoolean(scope) ?: false
        MLBridge.nativeSetTrainingMode(tg)
    }
}