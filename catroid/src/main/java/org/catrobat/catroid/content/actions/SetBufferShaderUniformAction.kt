package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import org.catrobat.catroid.content.RenderTextureManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula

class SetBufferShaderUniformAction : Action() {
    var scope: Scope? = null
    var bufferName: Formula? = null
    var uniformName: Formula? = null
    var val1: Formula? = null
    var val2: Formula? = null
    var val3: Formula? = null
    var paramCount: Int = 1

    override fun act(delta: Float): Boolean {
        val currentScope = scope ?: return true
        val buf = bufferName?.interpretString(currentScope) ?: return true
        val uni = uniformName?.interpretString(currentScope) ?: return true

        val v1 = val1?.interpretFloat(currentScope) ?: 0f
        val v2 = val2?.interpretFloat(currentScope) ?: 0f
        val v3 = val3?.interpretFloat(currentScope) ?: 0f

        val finalValue: Any = when (paramCount) {
            1 -> v1
            2 -> Vector2(v1, v2)
            else -> Vector3(v1, v2, v3)
        }

        RenderTextureManager.setBufferShaderUniform(buf, uni, finalValue)
        return true
    }
}
