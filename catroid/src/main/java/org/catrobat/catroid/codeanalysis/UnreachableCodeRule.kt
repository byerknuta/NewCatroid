package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.ProjectManager
import org.catrobat.catroid.content.bricks.*
import org.catrobat.catroid.formulaeditor.Formula

class UnreachableCodeRule(private val context: Context) : AnalysisRule {
    override fun analyze(brick: Brick): AnalysisResult? {
        val parent = brick.parent ?: return null
        val siblingBricks = parent.dragAndDropTargetList ?: return null
        val brickIndex = siblingBricks.indexOf(brick)

        if (brickIndex > 0) {
            val previousBrick = siblingBricks[brickIndex - 1]

            if (previousBrick is StopScriptBrick) {
                if (isHaltingStopScript(previousBrick)) {
                    return AnalysisResult(Severity.ERROR, context.getString(R.string.analysis_unreachable_stop_script))
                }
            }

            if (previousBrick is ForeverBrick) {
                if (!containsBreak(previousBrick)) {
                    return AnalysisResult(Severity.WARNING, context.getString(R.string.analysis_unreachable_after_forever))
                }
            }

            if (previousBrick is IfLogicBeginBrick) {
                if (endsWithHaltInList(previousBrick.nestedBricks) &&
                    endsWithHaltInList(previousBrick.secondaryNestedBricks)) {
                    return AnalysisResult(
                        Severity.ERROR,
                        context.getString(R.string.analysis_unreachable_both_branches_halt)
                    )
                }
            }

            if (previousBrick is RepeatUntilBrick) {
                val formula = previousBrick.getFormulaWithBrickField(Brick.BrickField.REPEAT_UNTIL_CONDITION)
                if (formula != null && isFormulaConstant(formula)) {
                    val isAlwaysFalse = try {
                        val scope = org.catrobat.catroid.content.Scope(
                            ProjectManager.getInstance().currentProject,
                            ProjectManager.getInstance().currentSprite,
                            com.badlogic.gdx.scenes.scene2d.actions.SequenceAction()
                        )
                        !formula.interpretBoolean(scope)
                    } catch (e: Exception) {
                        false
                    }
                    if (isAlwaysFalse && !containsBreak(previousBrick)) {
                        return AnalysisResult(Severity.WARNING, context.getString(R.string.analysis_unreachable_after_forever))
                    }
                }
            }
        }
        return null
    }

    private fun isHaltingStopScript(brick: StopScriptBrick): Boolean {
        try {
            val field = brick.javaClass.getDeclaredField("selection")
            field.isAccessible = true
            val value = field.get(brick)?.toString() ?: ""
            return !value.contains("other") && !value.contains("другие")
        } catch (e: Exception) {
            try {
                val field = brick.javaClass.getDeclaredField("stopType")
                field.isAccessible = true
                val value = field.get(brick)?.toString() ?: ""
                return !value.contains("other") && !value.contains("другие")
            } catch (e2: Exception) {
                return true
            }
        }
    }

    private fun endsWithHaltInList(bricks: List<Brick>): Boolean {
        if (bricks.isEmpty()) return false
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

    private fun containsBreak(compositeBrick: CompositeBrick): Boolean {
        for (nested in compositeBrick.nestedBricks) {
            if (nested is StopScriptBrick && isHaltingStopScript(nested)) return true
            if (nested is CompositeBrick && containsBreak(nested)) return true
        }
        if (compositeBrick.hasSecondaryList()) {
            for (nested in compositeBrick.secondaryNestedBricks) {
                if (nested is StopScriptBrick && isHaltingStopScript(nested)) return true
                if (nested is CompositeBrick && containsBreak(nested)) return true
            }
        }
        return false
    }

    private fun isFormulaConstant(formula: Formula?): Boolean {
        val root = formula?.root ?: return false
        return checkElementIsConstant(root)
    }

    private fun checkElementIsConstant(element: org.catrobat.catroid.formulaeditor.FormulaElement?): Boolean {
        if (element == null) return true
        when (element.type) {
            org.catrobat.catroid.formulaeditor.FormulaElement.ElementType.SENSOR,
            org.catrobat.catroid.formulaeditor.FormulaElement.ElementType.USER_VARIABLE,
            org.catrobat.catroid.formulaeditor.FormulaElement.ElementType.USER_LIST,
            org.catrobat.catroid.formulaeditor.FormulaElement.ElementType.COLLISION_FORMULA -> return false
            org.catrobat.catroid.formulaeditor.FormulaElement.ElementType.FUNCTION -> {
                if (element.value == "RAND") return false
            }
            else -> {}
        }
        return checkElementIsConstant(element.leftChild) &&
                checkElementIsConstant(element.rightChild) &&
                element.additionalChildren.all { checkElementIsConstant(it) }
    }
}
