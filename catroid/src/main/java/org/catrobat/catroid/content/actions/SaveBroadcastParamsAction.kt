package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.common.NewCatroidBroadcastManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.UserVariable

class SaveBroadcastParamsAction : TemporalAction() {
    var scope: Scope? = null
    var signalName: String? = null
    var userVariable: UserVariable? = null

    override fun update(percent: Float) {
        val signal = signalName ?: return

        val paramsJson = NewCatroidBroadcastManager.getParams(signal)
        userVariable?.value = paramsJson
    }

    override fun restart() {
        super.restart()
    }

    override fun reset() {
        super.reset()
        scope = null
        signalName = null
        userVariable = null
    }
}
