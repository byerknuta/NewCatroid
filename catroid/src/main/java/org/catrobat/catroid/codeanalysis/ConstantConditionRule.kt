package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.content.bricks.Brick
import org.catrobat.catroid.content.bricks.IfLogicBeginBrick
import org.catrobat.catroid.content.bricks.IfThenLogicBeginBrick
import org.catrobat.catroid.content.bricks.RepeatUntilBrick

class ConstantConditionRule(private val context2: Context) : AnalysisRule {

    override fun analyze(brick: Brick, context: GlobalAnalysisContext): AnalysisResult? {
        val formula = when (brick) {
            is IfThenLogicBeginBrick -> brick.getFormulaWithBrickField(Brick.BrickField.IF_CONDITION)
            is IfLogicBeginBrick -> brick.getFormulaWithBrickField(Brick.BrickField.IF_CONDITION)
            is RepeatUntilBrick -> brick.getFormulaWithBrickField(Brick.BrickField.REPEAT_UNTIL_CONDITION)
            else -> return null
        } ?: return null

        val incomingState = context.incomingStates[brick] ?: emptyMap()

        val staticResult = context.evaluateFormula(formula, incomingState)

        if (staticResult !is StaticValue.Bool) {
            return null
        }

        val evaluationResult = staticResult.value
        val conditionText = formula.getTrimmedFormulaString(context2)
        if (conditionText.isBlank()) return null

        return if (evaluationResult) {
            val message = when (brick) {
                is RepeatUntilBrick -> context2.getString(R.string.analysis_constant_condition_repeat_until_true, conditionText)
                else -> context2.getString(R.string.analysis_constant_condition_if_true, conditionText)
            }
            AnalysisResult(Severity.WARNING, message)
        } else {
            val message = when (brick) {
                is RepeatUntilBrick -> context2.getString(R.string.analysis_constant_condition_repeat_until_false, conditionText)
                else -> context2.getString(R.string.analysis_constant_condition_if_false, conditionText)
            }
            AnalysisResult(Severity.WARNING, message)
        }
    }
}
