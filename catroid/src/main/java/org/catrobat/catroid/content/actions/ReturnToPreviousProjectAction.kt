package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.stage.StageActivity

class ReturnToPreviousProjectAction : TemporalAction() {
    override fun update(percent: Float) {
        val stage = StageActivity.activeStageActivity?.get() ?: return
        stage.finish()
    }
}
