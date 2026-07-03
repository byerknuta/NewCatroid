package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.RenderTextureManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula

class SetMainRenderLoopsAction : Action() {
    var scope: Scope? = null
    var render2D: Formula? = null
    var renderFast2D: Formula? = null
    var render3D: Formula? = null

    override fun act(delta: Float): Boolean {
        val currentScope = scope ?: return true
        val r2d = render2D?.interpretFloat(currentScope) ?: 1f
        val rf2d = renderFast2D?.interpretFloat(currentScope) ?: 1f
        val r3d = render3D?.interpretFloat(currentScope) ?: 1f

        RenderTextureManager.isMain2DRenderEnabled = r2d != 0f
        RenderTextureManager.isMainFast2DRenderEnabled = rf2d != 0f
        RenderTextureManager.isMain3DRenderEnabled = r3d != 0f
        return true
    }
}
