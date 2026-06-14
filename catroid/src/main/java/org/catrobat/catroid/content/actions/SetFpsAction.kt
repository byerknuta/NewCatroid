package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class SetFpsAction : TemporalAction() {
    var scope: Scope? = null
    var fps: Formula? = null

    override fun update(percent: Float) {
        val fpsVal = fps?.interpretInteger(scope) ?: 0
        StageActivity.getActiveStageListener()?.threeDManager?.setTargetFps(fpsVal)
    }
}
