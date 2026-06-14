package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class SetScreenShaderAction : TemporalAction() {
    var scope: Scope? = null
    var vertex: Formula? = null
    var fragment: Formula? = null

    override fun update(percent: Float) {
        val vertStr = vertex?.interpretString(scope) ?: ""
        val fragStr = fragment?.interpretString(scope) ?: ""

        StageActivity.getActiveStageListener()?.threeDManager?.setCustomScreenShader(vertStr, fragStr)
    }
}
