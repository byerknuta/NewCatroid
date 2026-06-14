package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.stage.StageActivity

class SetObjectShaderUniformAction() : TemporalAction() {
    var scope: Scope? = null
    var objectIdFormula: Formula? = null
    var nameFormula: Formula? = null
    var val1Formula: Formula? = null
    var val2Formula: Formula? = null
    var val3Formula: Formula? = null

    override fun update(percent: Float) {
        val stageListener = StageActivity.getActiveStageListener() ?: return
        val threeDManager = stageListener.threeDManager ?: return

        val objId = objectIdFormula?.interpretString(scope) ?: ""
        val uName = nameFormula?.interpretString(scope) ?: ""

        val s1 = val1Formula?.interpretString(scope) ?: ""
        val s2 = val2Formula?.interpretString(scope) ?: ""
        val s3 = val3Formula?.interpretString(scope) ?: ""

        val v1 = val1Formula?.interpretFloat(scope) ?: 0f
        val v2 = val2Formula?.interpretFloat(scope) ?: 0f
        val v3 = val3Formula?.interpretFloat(scope) ?: 0f

        var paramCount = 0
        if (s1.isNotEmpty()) {
            paramCount = 1
            if (s2.isNotEmpty()) {
                paramCount = 2
                if (s3.isNotEmpty()) {
                    paramCount = 3
                }
            }
        }

        if (paramCount > 0) {
            threeDManager.setObjectShaderUniform(objId, uName, v1, v2, v3, paramCount)
        }
    }
}
