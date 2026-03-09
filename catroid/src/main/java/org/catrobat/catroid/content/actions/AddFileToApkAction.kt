package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.apkbuild.ApkToolboxManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import android.util.Log
import java.io.File

class AddFileToApkAction : TemporalAction() {
    var scope: Scope? = null

    var apkPathFormula: Formula? = null
    var sourcePathFormula: Formula? = null
    var destPathFormula: Formula? = null

    override fun update(percent: Float) {
        val project = scope?.project ?: return

        val apkPathStr = apkPathFormula?.interpretString(scope) ?: ""
        val sourcePathStr = sourcePathFormula?.interpretString(scope) ?: ""
        val destPathStr = destPathFormula?.interpretString(scope) ?: ""

        if (apkPathStr.isEmpty() || sourcePathStr.isEmpty()) return

        val apkFile = project.getFile(apkPathStr)
        val sourceFile = project.getFile(sourcePathStr)

        if (apkFile != null && apkFile.exists() && sourceFile != null && sourceFile.exists()) {
            val success = if (sourceFile.isDirectory) {
                ApkToolboxManager.addFolderToApk(
                    apkPath = apkFile.absolutePath,
                    sourceFolder = sourceFile,
                    destPathInApk = destPathStr
                )
            } else {
                ApkToolboxManager.addFileToApk(
                    apkPath = apkFile.absolutePath,
                    sourceFile = sourceFile,
                    pathInsideApk = destPathStr
                )
            }

            if (!success) {
                Log.e("AddFileAction", "Failed to add $sourcePathStr to APK")
            }
        } else {
            Log.e("AddFileAction", "APK or Source file not found")
        }
    }
}