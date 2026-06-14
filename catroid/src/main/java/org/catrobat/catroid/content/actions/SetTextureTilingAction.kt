package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class SetTextureTilingAction : TemporalAction() {
    var scope: Scope? = null
    var objectId: Formula? = null
    var scaleU: Formula? = null
    var scaleV: Formula? = null

    override fun update(percent: Float) {
        val sceneManager = StageActivity.getActiveStageListener()?.sceneManager ?: return
        val id = objectId?.interpretString(scope) ?: return

        val u = scaleU?.interpretDouble(scope)?.toFloat() ?: 1f
        val v = scaleV?.interpretDouble(scope)?.toFloat() ?: 1f

        val go = sceneManager.findGameObject(id)
        if (go != null) {
            var mat = go.getComponent(org.catrobat.catroid.raptor.MaterialComponent::class.java)
            if (mat == null) {
                mat = org.catrobat.catroid.raptor.MaterialComponent()
                go.addComponent(mat)
            }
            mat.uvScaleX = u
            mat.uvScaleY = v
        }

        StageActivity.getActiveStageListener()?.threeDManager?.setTextureTiling(id, u, v)
    }
}
