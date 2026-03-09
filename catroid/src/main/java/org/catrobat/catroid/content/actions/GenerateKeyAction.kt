package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.apkbuild.ApkToolboxManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula

class GenerateKeyAction : TemporalAction() {
    var scope: Scope? = null

    var filenameFormula: Formula? = null
    var passwordFormula: Formula? = null
    var aliasFormula: Formula? = null
    var commonNameFormula: Formula? = null

    override fun update(percent: Float) {
        val filename = filenameFormula?.interpretString(scope) ?: "release.jks"
        val password = passwordFormula?.interpretString(scope) ?: "123456"
        val alias = aliasFormula?.interpretString(scope) ?: "key0"
        val commonName = commonNameFormula?.interpretString(scope) ?: "User"

        val file = scope?.project?.getFile(filename)

        if (file != null) {
            ApkToolboxManager.generateKeyStore(
                outputPath = file.absolutePath,
                alias = alias,
                pass = password,
                commonName = commonName
            )
        }
    }
}