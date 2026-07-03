package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.RenderTextureManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula

class AddTextToBufferAction : Action() {
    var scope: Scope? = null
    var bufferName: Formula? = null
    var textName: Formula? = null

    override fun act(delta: Float): Boolean {
        val currentScope = scope ?: return true
        val buf = bufferName?.interpretString(currentScope) ?: return true
        val txt = textName?.interpretString(currentScope) ?: return true

        RenderTextureManager.addVariableTextToTarget(buf, txt)
        return true
    }
}
