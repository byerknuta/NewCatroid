package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class PromoteLightAction() : TemporalAction() {
    var scope: Scope? = null
    var lightId: Formula? = null

    override fun update(percent: Float) {
        val id = lightId?.interpretString(scope) ?: return
        if (id.isEmpty()) return

        val sm = StageActivity.getActiveStageListener()?.sceneManager ?: return
        sm.promoteLightToGameObject(id)
    }
}
