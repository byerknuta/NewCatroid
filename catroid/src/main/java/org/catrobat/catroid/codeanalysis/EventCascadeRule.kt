package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.content.bricks.Brick
import org.catrobat.catroid.content.bricks.BroadcastBrick
import org.catrobat.catroid.content.bricks.BroadcastReceiverBrick
import org.catrobat.catroid.content.bricks.BroadcastWaitBrick

class EventCascadeRule(private val context: Context) : AnalysisRule {
    override fun analyze(brick: Brick): AnalysisResult? {
        if (brick !is BroadcastBrick && brick !is BroadcastWaitBrick) return null

        val message = when (brick) {
            is BroadcastBrick -> brick.broadcastMessage
            is BroadcastWaitBrick -> brick.broadcastMessage
            else -> null
        } ?: return null

        val parentScript = brick.script
        if (parentScript is org.catrobat.catroid.content.BroadcastScript) {
            val receiverMessage = parentScript.broadcastMessage
            if (receiverMessage == message) {
                return AnalysisResult(
                    Severity.ERROR,
                    context.getString(R.string.analysis_event_cascade, message)
                )
            }
        }
        return null
    }
}
