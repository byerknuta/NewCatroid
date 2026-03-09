package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.apkbuild.ApkToolboxManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import android.util.Log

class UpdateManifestAction : TemporalAction() {
    var scope: Scope? = null

    var apkPathFormula: Formula? = null
    var packageNameFormula: Formula? = null
    var appNameFormula: Formula? = null
    var versionCodeFormula: Formula? = null
    var versionNameFormula: Formula? = null
    var minSdkFormula: Formula? = null
    var targetSdkFormula: Formula? = null
    var debuggableFormula: Formula? = null
    var permsAddFormula: Formula? = null
    var permsRemoveFormula: Formula? = null

    override fun update(percent: Float) {
        val project = scope?.project ?: return

        val apkPathStr = apkPathFormula?.interpretString(scope) ?: ""
        val apkFile = project.getFile(apkPathStr)

        if (apkFile == null || !apkFile.exists()) {
            Log.e("ManifestAction", "APK not found: $apkPathStr")
            return
        }

        fun getStr(f: Formula?): String? {
            val s = f?.interpretString(scope)
            return if (s.isNullOrEmpty()) null else s
        }

        fun getInt(f: Formula?): Int? = getStr(f)?.toIntOrNull()
        fun getBool(f: Formula?): Boolean? = getStr(f)?.toBooleanStrictOrNull()

        fun getList(f: Formula?): List<String>? {
            val raw = getStr(f) ?: return null
            return raw.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        }

        val config = ApkToolboxManager.ManifestConfig(
            packageName = getStr(packageNameFormula),
            appName = getStr(appNameFormula),
            versionCode = getInt(versionCodeFormula),
            versionName = getStr(versionNameFormula),
            minSdkVersion = getInt(minSdkFormula),
            targetSdkVersion = getInt(targetSdkFormula),
            debuggable = getBool(debuggableFormula),
            permissionsToAdd = getList(permsAddFormula),
            permissionsToRemove = getList(permsRemoveFormula)
        )

        ApkToolboxManager.updateManifest(apkFile.absolutePath, config)
    }
}