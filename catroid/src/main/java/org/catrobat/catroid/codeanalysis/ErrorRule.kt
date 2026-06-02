package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.content.bricks.Brick
import org.catrobat.catroid.content.bricks.WriteBaseBrick

class ErrorRule(private val context: Context) : AnalysisRule {
    override fun analyze(brick: Brick): AnalysisResult? {
        if (brick is WriteBaseBrick) {
            val formula = brick.getFormulaWithBrickField(Brick.BrickField.FIREBASE_ID) ?: return null

            if (isFormulaConstant(formula)) {
                val idString = formula.getTrimmedFormulaString(context)
                    .replace("'", "")
                    .replace("\"", "")
                    .trim()

                if (idString.isBlank()) {
                    return AnalysisResult(
                        Severity.ERROR,
                        context.getString(R.string.analysis_error_firebase_id_empty)
                    )
                }

                if (!idString.startsWith("https://") || !idString.endsWith(".firebaseio.com")) {
                    return AnalysisResult(
                        Severity.ERROR,
                        context.getString(R.string.analysis_error_firebase_id_invalid)
                    )
                }
            }
        }
        return null
    }

    private fun isFormulaConstant(formula: org.catrobat.catroid.formulaeditor.Formula?): Boolean {
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
