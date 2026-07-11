package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.content.bricks.*

class EmptyLoopRule(private val context: Context) : AnalysisRule {
    override fun analyze(brick: Brick, context2: GlobalAnalysisContext): AnalysisResult? {
        if (brick !is CompositeBrick) {
            return null
        }

        if (brick is IfLogicBeginBrick) {
            val ifBranchEmpty = brick.nestedBricks.isEmpty()
            val elseBranchEmpty = brick.secondaryNestedBricks.isEmpty()

            if (ifBranchEmpty && elseBranchEmpty) {
                return AnalysisResult(Severity.WARNING, context.getString(R.string.analysis_empty_loop_if_else_empty))
            }

            if (ifBranchEmpty && !elseBranchEmpty) {
                return AnalysisResult(
                    Severity.WARNING,
                    context.getString(R.string.analysis_empty_loop_if_empty_else_not)
                )
            }

            if (!ifBranchEmpty && elseBranchEmpty) {
                return AnalysisResult(
                    Severity.WARNING,
                    context.getString(R.string.analysis_empty_loop_if_not_else_empty)
                )
            }

            return null
        }

        if (brick is TryCatchFinallyBrick) {
            val tryBranchEmpty = brick.nestedBricks.isEmpty()
            val catchBranchEmpty = brick.secondaryNestedBricks.isEmpty()
            val finallyBranchEmpty = brick.thirdNestedBricks.isEmpty()

            if (tryBranchEmpty && catchBranchEmpty && finallyBranchEmpty) {
                return AnalysisResult(Severity.WARNING, context.getString(R.string.analysis_empty_loop_try_catch_finally_empty))
            }
            return null
        }

        if (brick.nestedBricks.isEmpty()) {
            val brickClass = brick.javaClass.simpleName
            val displayName = when (brickClass) {
                "ForeverBrick" -> "Forever"
                "RepeatBrick" -> "Repeat"
                "RepeatUntilBrick" -> "Repeat Until"
                "AsyncRepeatBrick" -> "Async Repeat"
                "IntervalRepeatBrick" -> "Interval Repeat"
                "ForVariableFromToBrick" -> "For Variable"
                "ForItemInUserListBrick" -> "For Each Item"
                "InstantBrick" -> "Execute Instant"
                "SpawnThreadBrick" -> "Start Thread"
                "RunAsSpriteBrick" -> "Run As"
                else -> brickClass.replace("Brick", "").replace("LogicBegin", "")
            }
            return AnalysisResult(
                Severity.WARNING,
                context.getString(R.string.analysis_empty_loop_empty_brick, displayName)
            )
        }
        return null
    }
}
