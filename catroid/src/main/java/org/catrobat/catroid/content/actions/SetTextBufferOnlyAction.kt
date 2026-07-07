package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.RenderTextureManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula

class SetTextBufferOnlyAction : TemporalAction() {
    var scope: Scope? = null
    var textName: Formula? = null
    var stateFormula: Formula? = null

    override fun update(percent: Float) {
        val currentScope = scope ?: return
        val txt = textName?.interpretString(currentScope) ?: return
        val state = stateFormula?.interpretInteger(currentScope) ?: 1

        RenderTextureManager.setTextBufferOnly(txt, state > 0)
    }

    override fun reset() {
        super.reset()
        scope = null
        textName = null
        stateFormula = null
    }
}
