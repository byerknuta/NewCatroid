package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.apkbuild.ApkToolboxManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import android.util.Log

class DeleteFromApkAction : TemporalAction() {
    var scope: Scope? = null

    var apkPathFormula: Formula? = null
    var deletePatternFormula: Formula? = null

    override fun update(percent: Float) {
        val project = scope?.project ?: return

        val apkPathStr = apkPathFormula?.interpretString(scope) ?: ""
        val deletePatternStr = deletePatternFormula?.interpretString(scope) ?: ""

        if (apkPathStr.isEmpty() || deletePatternStr.isEmpty()) return

        val apkFile = project.getFile(apkPathStr)

        if (apkFile != null && apkFile.exists()) {
            val success = ApkToolboxManager.deleteFromApk(
                apkPath = apkFile.absolutePath,
                pathPattern = deletePatternStr
            )

            if (!success) {
                Log.e("DeleteFromApk", "Failed to delete $deletePatternStr from APK")
            }
        }
    }
}