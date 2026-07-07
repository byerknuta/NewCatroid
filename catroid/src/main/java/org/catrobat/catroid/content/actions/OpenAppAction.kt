package org.catrobat.catroid.content.actions

import android.content.Intent
import android.widget.Toast
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.CatroidApplication
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class OpenAppAction : TemporalAction() {
    var scope: Scope? = null
    var packageName: Formula? = null

    override fun update(percent: Float) {
        val pkgStr = packageName?.interpretString(scope) ?: return
        if (pkgStr.trim().isEmpty()) return

        val context = CatroidApplication.getAppContext()
        val pm = context.packageManager

        try {
            val intent = pm.getLaunchIntentForPackage(pkgStr)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                StageActivity.activeStageActivity.get()?.runOnUiThread {
                    Toast.makeText(context, "Application '$pkgStr' is not installed", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
