package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.ProjectManager
import org.catrobat.catroid.content.bricks.Brick
import org.catrobat.catroid.content.bricks.BroadcastBrick
import org.catrobat.catroid.content.bricks.BroadcastReceiverBrick
import org.catrobat.catroid.content.bricks.BroadcastWaitBrick

class DeadScriptRule(private val context: Context) : AnalysisRule {
    override fun analyze(brick: Brick): AnalysisResult? {
        if (brick !is BroadcastReceiverBrick) return null

        val message = brick.broadcastMessage ?: return null
        if (message.isBlank()) return null

        val project = ProjectManager.getInstance().currentProject ?: return null

        for (scene in project.sceneList) {
            for (sprite in scene.spriteList) {
                val allBricks = mutableListOf<Brick>()
                for (script in sprite.scriptList) {
                    script.addToFlatList(allBricks)
                }

                val isSent = allBricks.any {
                    (it is BroadcastBrick && it.broadcastMessage == message) ||
                            (it is BroadcastWaitBrick && it.broadcastMessage == message)
                }
                if (isSent) {
                    return null
                }
            }
        }

        return AnalysisResult(
            Severity.WARNING,
            context.getString(R.string.analysis_dead_broadcast, message)
        )
    }
}
