package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.content.bricks.Brick
import org.catrobat.catroid.content.bricks.FormulaBrick
import org.catrobat.catroid.formulaeditor.FormulaElement
import org.catrobat.catroid.formulaeditor.Operators

class DivisionByZeroRule(private val context2: Context) : AnalysisRule {
    override fun analyze(brick: Brick, context: GlobalAnalysisContext): AnalysisResult? {
        if (brick !is FormulaBrick) return null

        try {
            for (formula in brick.allFormulaFieldsWithFormulas.values) {
                val root = formula.root ?: continue
                val hasDivByZero = scanForDivisionByZero(root, context)
                if (hasDivByZero) {
                    return AnalysisResult(
                        Severity.ERROR,
                        context2.getString(R.string.analysis_division_by_zero)
                    )
                }
            }
        } catch (_: Exception) {}
        return null
    }

    private fun scanForDivisionByZero(element: FormulaElement, context: GlobalAnalysisContext, depth: Int = 0): Boolean {
        if (depth > 50) return false

        if (element.type == FormulaElement.ElementType.OPERATOR) {
            val v: Any? = element.value
            val opStr = when (v) {
                is Operators -> v.name
                is Enum<*> -> v.name
                else -> v?.toString()?.uppercase() ?: ""
            }
            if (opStr == "DIVIDE" || opStr == "MOD") {
                val rightChild = element.rightChild
                if (rightChild != null && isElementConstantZero(rightChild, context)) {
                    return true
                }
            }
        }

        val left = element.leftChild?.let { scanForDivisionByZero(it, context, depth + 1) } ?: false
        val right = element.rightChild?.let { scanForDivisionByZero(it, context, depth + 1) } ?: false
        val additionals = element.additionalChildren.any { scanForDivisionByZero(it, context, depth + 1) }

        return left || right || additionals
    }

    private fun isElementConstantZero(element: FormulaElement, context: GlobalAnalysisContext): Boolean {
        val dummyFormula = org.catrobat.catroid.formulaeditor.Formula(element)
        val staticEval = context.evaluateFormula(dummyFormula, emptyMap())
        if (staticEval is StaticValue.Number && staticEval.value == 0.0) {
            return true
        }
        return false
    }
}
