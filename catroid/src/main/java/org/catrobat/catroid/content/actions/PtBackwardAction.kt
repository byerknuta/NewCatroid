package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtBackwardAction : TemporalAction() {
    var scope: Scope? = null
    var lossNameFormula: Formula? = null

    override fun update(percent: Float) {
        val lossName = lossNameFormula?.interpretString(scope) ?: "loss"
        MLBridge.nativeBackward(lossName)
    }
}