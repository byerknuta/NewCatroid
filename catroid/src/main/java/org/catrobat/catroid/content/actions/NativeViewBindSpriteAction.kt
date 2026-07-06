package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.common.NativeViewBindingManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class NativeViewBindSpriteAction : TemporalAction() {
    var scope: Scope? = null
    var viewId: Formula? = null
    var spriteName: Formula? = null
    var offsetX: Formula? = null
    var offsetY: Formula? = null
    var alignMode: Int = 1 // 0=Top-Left, 1=Center

    override fun update(percent: Float) {
        val stageListener = StageActivity.getActiveStageListener() ?: return
        val idStr = viewId?.interpretString(scope) ?: return
        val sName = spriteName?.interpretString(scope) ?: ""
        val ox = offsetX?.interpretFloat(scope) ?: 0.0f
        val oy = offsetY?.interpretFloat(scope) ?: 0.0f

        if (sName.isEmpty()) {
            NativeViewBindingManager.unbind(idStr)
            return
        }

        val targetSprite = stageListener.spritesFromStage.find { it.name == sName }
        if (targetSprite != null) {
            NativeViewBindingManager.bind(idStr, targetSprite, ox, oy, alignMode)
        }
    }

    override fun restart() {
        super.restart()
    }
}
