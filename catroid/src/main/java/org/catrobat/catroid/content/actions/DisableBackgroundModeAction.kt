package org.catrobat.catroid.content.actions

import android.content.Intent
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.CatroidApplication
import org.catrobat.catroid.common.NewCatroidBackgroundService
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.stage.StageActivity

class DisableBackgroundModeAction : TemporalAction() {
    var scope: Scope? = null

    override fun update(percent: Float) {
        val context = CatroidApplication.getAppContext() ?: return

        StageActivity.getActiveStageListener()?.isBackgroundModeEnabled = false

        val intent = Intent(context, NewCatroidBackgroundService::class.java)
        context.stopService(intent)
    }
}
