package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.content.bricks.*

class ResourceLeakRule(private val context: Context) : AnalysisRule {
    override fun analyze(brick: Brick): AnalysisResult? {
        val sprite = org.catrobat.catroid.ProjectManager.getInstance().currentSprite ?: return null

        val allSpriteBricks = mutableListOf<Brick>()
        for (script in sprite.scriptList) {
            script.addToFlatList(allSpriteBricks)
        }

        when (brick) {
            is LoadNNBrick -> {
                val hasUnload = allSpriteBricks.any { it is UnloadNNBrick }
                if (!hasUnload) {
                    return AnalysisResult(Severity.WARNING, context.getString(R.string.analysis_leak_nn))
                }
            }
            is RunVMBrick, is RunVm2Brick -> {
                val hasStopVm = allSpriteBricks.any { it is StopVMBrick }
                if (!hasStopVm) {
                    return AnalysisResult(Severity.WARNING, context.getString(R.string.analysis_leak_vm))
                }
            }
            is StartServerBrick -> {
                val hasStopServer = allSpriteBricks.any { it is StopServerBrick }
                if (!hasStopServer) {
                    return AnalysisResult(Severity.WARNING, context.getString(R.string.analysis_leak_server))
                }
            }
            is StartRecordingBrick -> {
                val hasStopRecording = allSpriteBricks.any { it is StopRecordingBrick }
                if (!hasStopRecording) {
                    return AnalysisResult(Severity.WARNING, context.getString(R.string.analysis_leak_recording))
                }
            }
        }
        return null
    }
}
