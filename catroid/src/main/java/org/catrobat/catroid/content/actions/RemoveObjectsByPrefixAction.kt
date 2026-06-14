package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class RemoveObjectsByPrefixAction : TemporalAction() {
    var scope: Scope? = null
    var prefixFormula: Formula? = null

    override fun update(percent: Float) {
        val stageListener = StageActivity.getActiveStageListener() ?: return
        val sceneManager = stageListener.sceneManager

        val prefix = prefixFormula?.interpretString(scope) ?: ""

        if (prefix.isNotEmpty()) {
            sceneManager?.removeObjectsByPrefix(prefix)
        }
    }
}
