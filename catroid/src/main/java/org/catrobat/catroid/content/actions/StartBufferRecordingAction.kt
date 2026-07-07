package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.utils.BufferVideoRecorder

class StartBufferRecordingAction : TemporalAction() {
    var scope: Scope? = null
    var bufferName: Formula? = null
    var fileName: Formula? = null
    var fpsFormula: Formula? = null
    var bitrateFormula: Formula? = null

    override fun update(percent: Float) {
        val currentScope = scope ?: return
        val buf = bufferName?.interpretString(currentScope) ?: return
        val fileStr = fileName?.interpretString(currentScope) ?: "record.mp4"
        val fps = fpsFormula?.interpretInteger(currentScope) ?: 30
        val bitrate = bitrateFormula?.interpretInteger(currentScope) ?: 2000000

        val destFile = currentScope.project?.getFile(fileStr) ?: return
        destFile.parentFile?.mkdirs()
        BufferVideoRecorder.startRecording(buf, destFile, fps, bitrate)
    }

    override fun reset() {
        super.reset()
        scope = null
        bufferName = null
        fileName = null
        fpsFormula = null
        bitrateFormula = null
    }
}
