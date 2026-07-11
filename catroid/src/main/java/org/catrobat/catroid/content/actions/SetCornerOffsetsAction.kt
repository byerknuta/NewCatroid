package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula

class SetCornerOffsetsAction : TemporalAction() {
    var scope: Scope? = null
    var tlx: Formula? = null
    var tly: Formula? = null
    var trx: Formula? = null
    var tryFormula: Formula? = null
    var brx: Formula? = null
    var bry: Formula? = null
    var blx: Formula? = null
    var bly: Formula? = null

    override fun update(percent: Float) {
        val sprite = scope?.sprite ?: return
        val look = sprite.look ?: return

        val tlXVal = tlx?.interpretFloat(scope) ?: 0f
        val tlYVal = tly?.interpretFloat(scope) ?: 0f
        val trXVal = trx?.interpretFloat(scope) ?: 0f
        val trYVal = tryFormula?.interpretFloat(scope) ?: 0f
        val brXVal = brx?.interpretFloat(scope) ?: 0f
        val brYVal = bry?.interpretFloat(scope) ?: 0f
        val blXVal = blx?.interpretFloat(scope) ?: 0f
        val blYVal = bly?.interpretFloat(scope) ?: 0f

        look.setCornerOffsets(tlXVal, tlYVal, trXVal, trYVal, brXVal, brYVal, blXVal, blYVal)
    }

    override fun reset() {
        super.reset()
        scope = null
        tlx = null
        tly = null
        trx = null
        tryFormula = null
        brx = null
        bry = null
        blx = null
        bly = null
    }
}
