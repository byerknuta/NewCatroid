package org.catrobat.catroid.content.actions

import android.util.Log
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class MLFileAction(private val isSave: Boolean) : TemporalAction() {
    var scope: Scope? = null
    var fileNameFormula: Formula? = null

    override fun update(percent: Float) {
        val name = fileNameFormula?.interpretString(scope) ?: return
        val file = scope?.project?.getFile(name) ?: return

        if (isSave) {
            file.parentFile?.mkdirs()
            MLBridge.nativeSaveModel(file.absolutePath)
        } else {
            if (file.exists()) {
                MLBridge.nativeLoadModel(file.absolutePath)
            } else {
                Log.e("Pocketensor", "Load model failed: File does not exist: ${file.absolutePath}")
            }
        }
    }
}
