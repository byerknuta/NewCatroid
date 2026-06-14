package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.stage.StageActivity

class SetObjectShaderAction() : TemporalAction() {
    var scope: Scope? = null
    var objectIdFormula: Formula? = null
    var vertexFormula: Formula? = null
    var fragmentFormula: Formula? = null

    override fun update(percent: Float) {
        val stageListener = StageActivity.getActiveStageListener() ?: return
        val threeDManager = stageListener.threeDManager ?: return

        val objId = objectIdFormula?.interpretString(scope) ?: ""
        val vertexCode = vertexFormula?.interpretString(scope) ?: ""
        val fragmentCode = fragmentFormula?.interpretString(scope) ?: ""

        threeDManager.setObjectCustomShader(objId, vertexCode, fragmentCode)
    }
}
