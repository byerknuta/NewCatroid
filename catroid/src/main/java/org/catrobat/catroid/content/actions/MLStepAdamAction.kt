package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class MLStepAdamAction : TemporalAction() {
    var scope: Scope? = null
    var lr: Formula? = null

    override fun update(percent: Float) {
        val lrVal = lr?.interpretFloat(scope) ?: 0.001f
        MLBridge.nativeStepAdam(lrVal)
    }
}