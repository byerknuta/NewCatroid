package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.RenderTextureManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula

class SetBufferShaderAction : Action() {
    var scope: Scope? = null
    var bufferName: Formula? = null
    var vertexCode: Formula? = null
    var fragmentCode: Formula? = null

    override fun act(delta: Float): Boolean {
        val currentScope = scope ?: return true
        val buf = bufferName?.interpretString(currentScope) ?: return true
        val vsh = vertexCode?.interpretString(currentScope)
        val fsh = fragmentCode?.interpretString(currentScope)

        RenderTextureManager.setBufferShader(buf, vsh, fsh)
        return true
    }
}
