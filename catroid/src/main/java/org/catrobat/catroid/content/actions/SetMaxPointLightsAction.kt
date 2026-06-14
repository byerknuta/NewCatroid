package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class SetMaxPointLightsAction() : TemporalAction() {
    var scope: Scope? = null
    var maxLights: Formula? = null

    override fun update(percent: Float) {
        val engine = StageActivity.getActiveStageListener()?.sceneManager?.engine ?: return
        val limit = maxLights?.interpretInteger(scope) ?: 5

        engine.setMaxActivePointLights(limit)
    }
}
