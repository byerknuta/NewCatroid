package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class ApplyShaderToImageAction() : TemporalAction() {
    var scope: Scope? = null
    var filename: Formula? = null
    var vertexCode: Formula? = null
    var fragmentCode: Formula? = null

    override fun update(percent: Float) {
        val file = filename?.interpretString(scope) ?: return
        val vsh = vertexCode?.interpretString(scope) ?: ""
        val fsh = fragmentCode?.interpretString(scope) ?: ""

        if (file.isEmpty()) return

        val sm = StageActivity.getActiveStageListener()?.sceneManager ?: return
        sm.applyShaderToImage(file, vsh, fsh)
    }
}
