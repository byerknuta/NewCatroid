package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.RenderTextureManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula

class SetBufferEffectsAction : Action() {
    var scope: Scope? = null
    var bufferName: Formula? = null
    var useVfx: Formula? = null
    var useMipmapping: Formula? = null

    override fun act(delta: Float): Boolean {
        val currentScope = scope ?: return true
        val buf = bufferName?.interpretString(currentScope) ?: return true
        val vfx = useVfx?.interpretFloat(currentScope) ?: 0f
        val mip = useMipmapping?.interpretFloat(currentScope) ?: 0f

        RenderTextureManager.setBufferPostProcessing(buf, vfx != 0f)
        RenderTextureManager.setBufferMipmapping(buf, mip != 0f)
        return true
    }
}
