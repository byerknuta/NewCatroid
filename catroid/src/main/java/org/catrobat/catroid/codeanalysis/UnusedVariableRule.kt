package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.content.bricks.*
import org.catrobat.catroid.formulaeditor.FormulaElement

class UnusedVariableRule(private val context: Context) : AnalysisRule {
    override fun analyze(brick: Brick): AnalysisResult? {
        val sprite = org.catrobat.catroid.ProjectManager.getInstance().currentSprite ?: return null

        val varName = when (brick) {
            is SetVariableBrick -> brick.userVariable?.name
            is ChangeVariableBrick -> brick.userVariable?.name
            else -> null
        } ?: return null

        val allBricks = mutableListOf<Brick>()
        for (script in sprite.scriptList) {
            script.addToFlatList(allBricks)
        }

        val isRead = allBricks.any { usesVariable(it, varName) }
        if (!isRead) {
            return AnalysisResult(
                Severity.WARNING,
                context.getString(R.string.analysis_unused_variable, varName)
            )
        }
        return null
    }

    private fun usesVariable(brick: Brick, varName: String): Boolean {
        if (brick is ChangeVariableBrick && brick.userVariable?.name == varName) {
            return true
        }

        if (brick is FormulaBrick && brick !is SetVariableBrick) {
            for (formula in brick.allFormulaFieldsWithFormulas.values) {
                val root = formula.root ?: continue
                if (hasVariableReference(root, varName)) {
                    return true
                }
            }
        }
        return false
    }

    private fun hasVariableReference(element: FormulaElement, varName: String): Boolean {
        if (element.type == org.catrobat.catroid.formulaeditor.FormulaElement.ElementType.USER_VARIABLE) {
            if (element.value?.toString() == varName) return true
        }
        val left = element.leftChild?.let { hasVariableReference(it, varName) } ?: false
        val right = element.rightChild?.let { hasVariableReference(it, varName) } ?: false
        val additionals = element.additionalChildren.any { hasVariableReference(it, varName) }
        return left || right || additionals
    }
}
