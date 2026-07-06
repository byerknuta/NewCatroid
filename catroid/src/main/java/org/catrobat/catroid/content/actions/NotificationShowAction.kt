package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.utils.NewCatroidNotificationManager

class NotificationShowAction : TemporalAction() {
    var scope: Scope? = null
    var notifId: Formula? = null
    var delaySeconds: Formula? = null

    override fun update(percent: Float) {
        val idStr = NewCatroidNotificationManager.cleanStringId(notifId?.interpretString(scope) ?: "")
        if (idStr.isEmpty()) return

        val delay = delaySeconds?.interpretDouble(scope) ?: 0.0
        NewCatroidNotificationManager.showOrSchedule(idStr, delay)
    }
}
