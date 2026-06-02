package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.content.bricks.Brick
import org.catrobat.catroid.content.bricks.FormulaBrick
import org.catrobat.catroid.formulaeditor.FormulaElement
import org.catrobat.catroid.formulaeditor.Operators

class DivisionByZeroRule(private val context: Context) : AnalysisRule {
    override fun analyze(brick: Brick): AnalysisResult? {
        if (brick !is FormulaBrick) return null

        for (formula in brick.allFormulaFieldsWithFormulas.values) {
            val root = formula.root ?: continue
            val hasDivByZero = scanForDivisionByZero(root)
            if (hasDivByZero) {
                return AnalysisResult(
                    Severity.ERROR,
                    context.getString(R.string.analysis_division_by_zero)
                )
            }
        }
        return null
    }

    private fun scanForDivisionByZero(element: FormulaElement): Boolean {
        if (element.type == FormulaElement.ElementType.OPERATOR) {
            val opValue = element.value?.toString() ?: ""
            if (opValue == Operators.DIVIDE.toString() || opValue == Operators.MOD.toString()) {
                val rightChild = element.rightChild
                if (rightChild != null && isElementConstantZero(rightChild)) {
                    return true
                }
            }
        }

        val left = element.leftChild?.let { scanForDivisionByZero(it) } ?: false
        val right = element.rightChild?.let { scanForDivisionByZero(it) } ?: false
        val additionals = element.additionalChildren.any { scanForDivisionByZero(it) }

        return left || right || additionals
    }

    private fun isElementConstantZero(element: FormulaElement): Boolean {
        if (element.type == FormulaElement.ElementType.NUMBER) {
            val value = element.value?.toString()?.toDoubleOrNull()
            if (value == 0.0) return true
        }
        if (checkElementIsConstant(element)) {
            try {
                val dummyFormula = org.catrobat.catroid.formulaeditor.Formula(element)
                val scope = org.catrobat.catroid.content.Scope(
                    org.catrobat.catroid.ProjectManager.getInstance().currentProject,
                    org.catrobat.catroid.ProjectManager.getInstance().currentSprite,
                    com.badlogic.gdx.scenes.scene2d.actions.SequenceAction()
                )
                val value = dummyFormula.interpretDouble(scope)
                if (value == 0.0) return true
            } catch (_: Exception) {
            }
        }
        return false
    }

    private fun checkElementIsConstant(element: FormulaElement?): Boolean {
        if (element == null) return true
        when (element.type) {
            FormulaElement.ElementType.SENSOR,
            FormulaElement.ElementType.USER_VARIABLE,
            FormulaElement.ElementType.USER_LIST,
            FormulaElement.ElementType.COLLISION_FORMULA -> return false
            FormulaElement.ElementType.FUNCTION -> {
                if (element.value == "RAND") return false
            }
            else -> {}
        }
        return checkElementIsConstant(element.leftChild) &&
                checkElementIsConstant(element.rightChild) &&
                element.additionalChildren.all { checkElementIsConstant(it) }
    }
}
