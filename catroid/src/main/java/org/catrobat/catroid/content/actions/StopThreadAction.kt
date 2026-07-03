package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.utils.ActionThreadRegistry

class StopThreadAction : Action() {
    var scope: Scope? = null
    var threadIdFormula: Formula? = null

    override fun act(delta: Float): Boolean {
        val rawId = threadIdFormula?.interpretString(scope) ?: return true
        ActionThreadRegistry.stopThread(rawId)
        return true
    }
}
