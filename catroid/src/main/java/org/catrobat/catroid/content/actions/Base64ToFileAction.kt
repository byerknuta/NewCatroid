package org.catrobat.catroid.content.actions

import android.util.Base64
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import java.io.FileOutputStream

class Base64ToFileAction : TemporalAction() {
    var scope: Scope? = null
    var base64String: Formula? = null
    var destinationFileName: Formula? = null

    override fun update(percent: Float) {
        val b64 = base64String?.interpretString(scope) ?: ""
        val dest = destinationFileName?.interpretString(scope) ?: ""

        if (b64.isEmpty() || dest.isEmpty()) return

        try {
            val file = scope?.project?.getFile(dest) ?: return

            val bytes = Base64.decode(b64, Base64.DEFAULT)

            FileOutputStream(file).use { fos ->
                fos.write(bytes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
