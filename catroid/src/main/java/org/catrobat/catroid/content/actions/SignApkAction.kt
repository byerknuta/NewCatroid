package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.apkbuild.ApkToolboxManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import android.util.Log

class SignApkAction : TemporalAction() {
    var scope: Scope? = null

    var inputApkFormula: Formula? = null
    var outputApkFormula: Formula? = null
    var keystoreFormula: Formula? = null
    var passwordFormula: Formula? = null
    var aliasFormula: Formula? = null

    override fun update(percent: Float) {
        val inputFile = inputApkFormula?.interpretString(scope) ?: "game-unsigned.apk"
        val outputFile = outputApkFormula?.interpretString(scope) ?: "game-signed.apk"

        var keystoreFile = keystoreFormula?.interpretString(scope) ?: ""
        var pass = passwordFormula?.interpretString(scope) ?: ""
        var alias = aliasFormula?.interpretString(scope) ?: ""

        val context = org.catrobat.catroid.CatroidApplication.getAppContext()

        val inputF = scope?.project?.getFile(inputFile)
        val outputF = scope?.project?.getFile(outputFile)

        val keyF = if (keystoreFile.isNotEmpty()) scope?.project?.getFile(keystoreFile) else null

        if (inputF != null && outputF != null) {
            ApkToolboxManager.signApk(
                context = context,
                inputApkPath = inputF.absolutePath,
                outputApkPath = outputF.absolutePath,
                keyStorePath = keyF?.absolutePath, // Может быть null
                keyAlias = if (alias.isNotEmpty()) alias else null,
                keyPass = if (pass.isNotEmpty()) pass else null
            )
        } else {
            Log.e("SignApkAction", "Input or Output file path is invalid")
        }
    }
}