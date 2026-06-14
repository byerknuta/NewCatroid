package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class AttachToCameraAction() : TemporalAction() {
    var scope: Scope? = null
    var objectName: Formula? = null

    override fun update(percent: Float) {
        val targetName = objectName?.interpretString(scope) ?: ""
        if (targetName.isEmpty()) return

        val sm = StageActivity.getActiveStageListener()?.sceneManager ?: return
        sm.attachObjectToCamera(targetName)
    }
}
