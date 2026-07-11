package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula

class PlaySpritesheetAction : TemporalAction() {
    var scope: Scope? = null
    var rows: Formula? = null
    var cols: Formula? = null
    var selectedRow: Formula? = null
    var framesCount: Formula? = null
    var speed: Formula? = null

    override fun update(percent: Float) {
        val sprite = scope?.sprite ?: return
        val look = sprite.look ?: return

        val r = rows?.interpretInteger(scope) ?: 1
        val c = cols?.interpretInteger(scope) ?: 1
        val row = selectedRow?.interpretInteger(scope) ?: 0
        val fc = framesCount?.interpretInteger(scope) ?: 1
        val s = speed?.interpretFloat(scope) ?: 0.1f

        look.playSpritesheet(r, c, row, fc, s)
    }

    override fun reset() {
        super.reset()
        scope = null
        rows = null
        cols = null
        selectedRow = null
        framesCount = null
        speed = null
    }
}
