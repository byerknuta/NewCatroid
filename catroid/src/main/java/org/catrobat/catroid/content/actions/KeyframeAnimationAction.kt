package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class KeyframeAnimationAction : TemporalAction() {
    var scope: Scope? = null
    var objectId: Formula? = null
    var actionType: Int = 0 // 0: Play, 1: Stop, 2: Set Time
    var timeFormula: Formula? = null

    override fun update(percent: Float) {
        val sceneManager = StageActivity.getActiveStageListener()?.sceneManager ?: return
        val id = objectId?.interpretString(scope) ?: return

        if (id.isEmpty()) return

        when (actionType) {
            0 -> sceneManager.playKeyframeAnimation(id)
            1 -> sceneManager.stopKeyframeAnimation(id)
            2 -> {
                val time = timeFormula?.interpretFloat(scope) ?: 0f
                sceneManager.setKeyframeAnimationTime(id, time)
            }
        }
    }
}