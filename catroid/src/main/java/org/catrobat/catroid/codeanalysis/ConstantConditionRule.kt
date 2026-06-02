package org.catrobat.catroid.codeanalysis

import android.content.Context
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import org.catrobat.catroid.ProjectManager
import org.catrobat.catroid.R
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.content.bricks.Brick
import org.catrobat.catroid.content.bricks.IfLogicBeginBrick
import org.catrobat.catroid.content.bricks.IfThenLogicBeginBrick
import org.catrobat.catroid.content.bricks.RepeatUntilBrick
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.formulaeditor.FormulaElement

class ConstantConditionRule(private val context: Context) : AnalysisRule {
    override fun analyze(brick: Brick): AnalysisResult? {
        val project = ProjectManager.getInstance().currentProject ?: return null
        val sprite = ProjectManager.getInstance().currentSprite ?: return null

        val analysisScope = Scope(project, sprite, SequenceAction())

        val formula = when (brick) {
            is IfThenLogicBeginBrick -> brick.getFormulaWithBrickField(Brick.BrickField.IF_CONDITION)
            is IfLogicBeginBrick -> brick.getFormulaWithBrickField(Brick.BrickField.IF_CONDITION)
            is RepeatUntilBrick -> brick.getFormulaWithBrickField(Brick.BrickField.REPEAT_UNTIL_CONDITION)
            else -> return null
        } ?: return null

        if (!isFormulaConstant(formula)) {
            return null
        }

        val evaluationResult: Boolean = try {
            formula.interpretBoolean(analysisScope)
        } catch (e: Exception) {
            return null
        }

        val conditionText = formula.getTrimmedFormulaString(context)
        if (conditionText.isBlank()) return null

        return if (evaluationResult) {
            val message = when (brick) {
                is RepeatUntilBrick -> context.getString(R.string.analysis_constant_condition_repeat_until_true, conditionText)
                else -> context.getString(R.string.analysis_constant_condition_if_true, conditionText)
            }
            AnalysisResult(Severity.WARNING, message)
        } else {
            val message = when (brick) {
                is RepeatUntilBrick -> context.getString(R.string.analysis_constant_condition_repeat_until_false, conditionText)
                else -> context.getString(R.string.analysis_constant_condition_if_false, conditionText)
            }
            AnalysisResult(Severity.WARNING, message)
        }
    }

    private fun isFormulaConstant(formula: Formula?): Boolean {
        val root = formula?.root ?: return false
        return checkElementIsConstant(root)
    }

    private fun checkElementIsConstant(element: FormulaElement?): Boolean {
        if (element == null) return true

        when (element.type) {
            FormulaElement.ElementType.SENSOR,
            FormulaElement.ElementType.USER_VARIABLE,
            FormulaElement.ElementType.USER_LIST,
            FormulaElement.ElementType.COLLISION_FORMULA -> return false

            FormulaElement.ElementType.FUNCTION -> {
                if (element.value == "RAND") {
                    return false
                }
            }
            else -> {}
        }

        return checkElementIsConstant(element.leftChild) &&
                checkElementIsConstant(element.rightChild) &&
                element.additionalChildren.all { checkElementIsConstant(it) }
    }
}
