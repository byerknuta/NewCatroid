package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class CameraTrackingAction() : TemporalAction() {
    var scope: Scope? = null
    var objectId: Formula? = null
    var mode: Int = 0
    var px: Formula? = null
    var py: Formula? = null
    var pz: Formula? = null
    var ry: Formula? = null
    var rp: Formula? = null
    var rr: Formula? = null

    override fun update(percent: Float) {
        val target = objectId?.interpretString(scope) ?: return

        val fPx = px?.interpretFloat(scope) ?: 0f
        val fPy = py?.interpretFloat(scope) ?: 0f
        val fPz = pz?.interpretFloat(scope) ?: 0f
        val fRy = ry?.interpretFloat(scope) ?: 0f
        val fRp = rp?.interpretFloat(scope) ?: 0f
        val fRr = rr?.interpretFloat(scope) ?: 0f

        val sm = StageActivity.getActiveStageListener()?.sceneManager ?: return
        sm.setCameraTracking(target, mode, fPx, fPy, fPz, fRy, fRp, fRr)
    }
}
