package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class ThreedAlignNormalAction : TemporalAction() {
    var scope: Scope? = null
    var objId: Formula? = null
    var nx: Formula? = null
    var ny: Formula? = null
    var nz: Formula? = null
    override fun update(percent: Float) {
        val tm = StageActivity.getActiveStageListener().threeDManager ?: return
        try {
            val id = objId!!.interpretString(scope)
            if (id.isEmpty()) return
            tm.alignObjectToNormal(
                id,
                nx!!.interpretFloat(scope),
                ny!!.interpretFloat(scope),
                nz!!.interpretFloat(scope)
            )
        } catch (ignored: Exception) {
        }
    }
}