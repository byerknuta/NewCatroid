package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.content.bricks.*

class ParameterValidationRule(private val context: Context) : AnalysisRule {
    override fun analyze(brick: Brick, context2: GlobalAnalysisContext): AnalysisResult? {
        if (brick is WaitBrick) {
            var currentParent = brick.parent
            while (currentParent != null) {
                if (currentParent is AsyncRepeatBrick) {
                    return AnalysisResult(Severity.WARNING, context.getString(R.string.analysis_wait_inside_async))
                }
                currentParent = currentParent.parent
            }
        }

        if (brick is SetShadowQualityBrick) {
            val formula = brick.getFormulaWithBrickField(Brick.BrickField.SHADOW_RESOLUTION)
            if (formula != null && isFormulaConstant(formula)) {
                val valueStr = formula.getTrimmedFormulaString(context).replace("'", "").trim()
                val value = valueStr.toIntOrNull()
                if (value != null && value > 0) {
                    val isPowerOfTwo = (value and (value - 1)) == 0
                    if (!isPowerOfTwo) {
                        return AnalysisResult(
                            Severity.WARNING,
                            context.getString(R.string.analysis_shadow_quality_not_power_of_two, valueStr)
                        )
                    }
                }
            }
        }

        if (brick is Set3dScaleBrick) {
            val formulas = brick.allFormulaFieldsWithFormulas.values.toList()
            if (formulas.size >= 3) {
                val fX = brick.getFormulaWithBrickField(Brick.BrickField.VALUE_2)
                val fY = brick.getFormulaWithBrickField(Brick.BrickField.VALUE_3)
                val fZ = brick.getFormulaWithBrickField(Brick.BrickField.VALUE_4)
                if (isFormulaConstant(fX) && isFormulaConstant(fY) && isFormulaConstant(fZ)) {
                    val sX = fX.getTrimmedFormulaString(context)
                    val sY = fY.getTrimmedFormulaString(context)
                    val sZ = fZ.getTrimmedFormulaString(context)
                    if (sX != sY || sY != sZ) {
                        return AnalysisResult(
                            Severity.WARNING,
                            context.getString(R.string.analysis_scale_3d_unequal, sX, sY, sZ)
                        )
                    }
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
