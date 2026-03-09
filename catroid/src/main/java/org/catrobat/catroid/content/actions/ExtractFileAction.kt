package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.apkbuild.ApkToolboxManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import android.util.Log

class ExtractFileAction : TemporalAction() {
    var scope: Scope? = null

    var apkPathFormula: Formula? = null
    var innerPathFormula: Formula? = null
    var destPathFormula: Formula? = null

    override fun update(percent: Float) {
        val project = scope?.project ?: return

        val apkPathStr = apkPathFormula?.interpretString(scope) ?: ""
        val innerPathStr = innerPathFormula?.interpretString(scope) ?: ""
        val destPathStr = destPathFormula?.interpretString(scope) ?: ""

        if (apkPathStr.isEmpty() || innerPathStr.isEmpty() || destPathStr.isEmpty()) {
            return
        }

        val apkFile = project.getFile(apkPathStr)
        val destFile = project.getFile(destPathStr)

        if (apkFile != null && apkFile.exists()) {
            val success = ApkToolboxManager.extractFileFromApk(
                apkPath = apkFile.absolutePath,
                pathInsideApk = innerPathStr,
                outputLocalPath = destFile.absolutePath
            )

            if (!success) {
                Log.e("ExtractAction", "Failed to extract $innerPathStr from $apkPathStr")
            }
        } else {
            Log.e("ExtractAction", "Source APK not found: $apkPathStr")
        }
    }
}