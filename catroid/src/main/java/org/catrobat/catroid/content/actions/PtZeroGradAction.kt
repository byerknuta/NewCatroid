package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.ml.MLBridge

class PtZeroGradAction : TemporalAction() {
    override fun update(percent: Float) {
        MLBridge.nativeZeroGrad()
    }
}
