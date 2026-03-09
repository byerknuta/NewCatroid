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
        Log.d("TEST_123", "called")
        val name = fileNameFormula?.interpretString(scope) ?: return
        val file = scope?.project?.getFile(name)
        Log.d("TEST_123", "interpreted")

        if (!isSave) {
            Log.d("TEST_123", "loading")
            file?.let {
                if (it.exists()) {
                    Log.d("TEST_123", "native load")
                    MLBridge.nativeLoadModel(it.absolutePath)
                }
                Log.d("TEST_123", "end loading")
            }
        } else {
            file?.parentFile?.mkdirs()
            Log.d("TEST_123", "saving native call: " + file?.absolutePath)
            MLBridge.nativeSaveModel(file?.absolutePath)
        }
    }
}