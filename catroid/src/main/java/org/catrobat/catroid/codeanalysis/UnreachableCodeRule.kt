package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.content.bricks.*

class UnreachableCodeRule(private val context2: Context) : AnalysisRule {
    override fun analyze(brick: Brick, context: GlobalAnalysisContext): AnalysisResult? {
        val parent = brick.parent ?: return null
        val siblingBricks = parent.dragAndDropTargetList ?: return null
        val brickIndex = siblingBricks.indexOf(brick)

        if (brickIndex > 0) {
            val previousBrick = siblingBricks[brickIndex - 1]

            if (previousBrick is StopScriptBrick) {
                if (isHaltingStopScript(previousBrick)) {
                    return AnalysisResult(Severity.ERROR, context2.getString(R.string.analysis_unreachable_stop_script))
                }
            }

            if (previousBrick is ForeverBrick) {
                if (!containsBreak(previousBrick)) {
                    return AnalysisResult(Severity.WARNING, context2.getString(R.string.analysis_unreachable_after_forever))
                }
            }

            if (previousBrick is IfLogicBeginBrick) {
                if (endsWithHaltInList(previousBrick.nestedBricks) &&
                    endsWithHaltInList(previousBrick.secondaryNestedBricks)) {
                    return AnalysisResult(
                        Severity.ERROR,
                        context2.getString(R.string.analysis_unreachable_both_branches_halt)
                    )
                }
            }

            if (previousBrick is RepeatUntilBrick) {
                val formula = previousBrick.getFormulaWithBrickField(Brick.BrickField.REPEAT_UNTIL_CONDITION)
                if (formula != null) {
                    val state = context.incomingStates[previousBrick] ?: emptyMap()
                    val staticEval = context.evaluateFormula(formula, state)
                    if (staticEval is StaticValue.Bool && !staticEval.value && !containsBreak(previousBrick)) {
                        return AnalysisResult(Severity.WARNING, context2.getString(R.string.analysis_unreachable_after_forever))
                    }
                }
            }
        }
        return null
    }

    private fun isHaltingStopScript(brick: StopScriptBrick): Boolean {
        return try {
            val field = brick.javaClass.getDeclaredField("spinnerSelection")
            field.isAccessible = true
            val selection = field.get(brick) as? Int ?: return true

            val stopOtherValue = try {
                val brickValuesClass = Class.forName("org.catrobat.catroid.common.BrickValues")
                val stopOtherField = brickValuesClass.getDeclaredField("STOP_OTHER_SCRIPTS")
                stopOtherField.isAccessible = true
                stopOtherField.get(null) as? Int ?: 2
            } catch (_: Exception) {
                2
            }

            selection != stopOtherValue
        } catch (_: Exception) {
            true
        }
    }

    private fun endsWithHaltInList(bricks: List<Brick>?): Boolean {
        if (bricks == null || bricks.isEmpty()) return false
        val last = bricks.last()
        if (last is StopScriptBrick && isHaltingStopScript(last)) return true
        if (last is CompositeBrick && last !is IfThenLogicBeginBrick) {
            if (last is IfLogicBeginBrick) {
                return endsWithHaltInList(last.nestedBricks) && endsWithHaltInList(last.secondaryNestedBricks)
            }
            if (last is ForeverBrick) return !containsBreak(last)
        }
        return false
    }

    private fun containsBreak(compositeBrick: CompositeBrick, depth: Int = 0): Boolean {
        if (depth > 50) return false

        val nested = compositeBrick.nestedBricks
        if (nested != null) {
            for (brick in nested) {
                if (brick is StopScriptBrick && isHaltingStopScript(brick)) return true
                if (brick is CompositeBrick && containsBreak(brick, depth + 1)) return true
            }
        }
        if (compositeBrick.hasSecondaryList()) {
            val secondary = compositeBrick.secondaryNestedBricks
            if (secondary != null) {
                for (brick in secondary) {
                    if (brick is StopScriptBrick && isHaltingStopScript(brick)) return true
                    if (brick is CompositeBrick && containsBreak(brick, depth + 1)) return true
                }
            }
        }
        return false
    }
}
