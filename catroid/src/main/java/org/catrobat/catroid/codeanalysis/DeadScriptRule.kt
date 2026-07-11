package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.content.bricks.Brick
import org.catrobat.catroid.content.bricks.BroadcastBrick
import org.catrobat.catroid.content.bricks.BroadcastReceiverBrick
import org.catrobat.catroid.content.bricks.BroadcastWaitBrick

class DeadScriptRule(private val context2: Context) : AnalysisRule {
    override fun analyze(brick: Brick, context: GlobalAnalysisContext): AnalysisResult? {
        if (brick is BroadcastReceiverBrick) {
            val message = brick.broadcastMessage ?: return null
            if (message.isBlank()) return null

            val isSent = context.allBroadcastsSent.contains(message)
            if (!isSent) {
                return AnalysisResult(
                    Severity.WARNING,
                    context2.getString(R.string.analysis_dead_broadcast, message)
                )
            }
        }

        if (brick is BroadcastBrick || brick is BroadcastWaitBrick) {
            val message = when (brick) {
                is BroadcastBrick -> brick.broadcastMessage
                is BroadcastWaitBrick -> brick.broadcastMessage
                else -> null
            } ?: return null
            if (message.isBlank()) return null

            val isReceived = context.variablesRead.contains(message) ||
                    context.allBroadcastsSent.any { it == message }

            val isRegisteredReceiver = context.allBroadcastsSent.contains(message)
            val isSomeoneListening = context.allBroadcastsReceived.contains(message)

            if (!isSomeoneListening) {
                return AnalysisResult(
                    Severity.WARNING,
                    context2.getString(R.string.analysis_unused_broadcast_sent, message)
                )
            }
        }

        return null
    }
}
