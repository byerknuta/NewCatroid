package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class SetActiveAction : TemporalAction() {
    var scope: Scope? = null
    var objectName: Formula? = null
    var activeState: Boolean = true

    override fun update(percent: Float) {
        val name = objectName?.interpretString(scope)
        if (name.isNullOrEmpty()) return

        val sceneManager = StageActivity.getActiveStageListener()?.sceneManager ?: return
        val gameObject = sceneManager.findObjectByName(name)

        if (gameObject != null) {
            sceneManager.setObjectActive(gameObject, activeState)
        } else {
            StageActivity.getActiveStageListener().threeDManager.setObjectVisibility(name, activeState)
        }
    }
}