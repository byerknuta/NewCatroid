package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.ProjectManager
import org.catrobat.catroid.content.bricks.Brick
import org.catrobat.catroid.content.bricks.FormulaBrick
import org.catrobat.catroid.formulaeditor.FormulaElement

class UndefinedReferenceRule(private val context: Context) : AnalysisRule {
    override fun analyze(brick: Brick, context2: GlobalAnalysisContext): AnalysisResult? {
        if (brick !is FormulaBrick) return null

        val project = ProjectManager.getInstance().currentProject ?: return null
        val sprite = ProjectManager.getInstance().currentSprite ?: return null

        val declaredVariables = (project.userVariables?.map { it.name } ?: emptyList()) +
                (sprite.userVariables?.map { it.name } ?: emptyList())

        val declaredLists = (project.userLists?.map { it.name } ?: emptyList()) +
                (sprite.userLists?.map { it.name } ?: emptyList())

        for (formula in brick.allFormulaFieldsWithFormulas.values) {
            val root = formula.root ?: continue
            val error = scanForUndefined(root, declaredVariables, declaredLists)
            if (error != null) {
                return error
            }
        }

        return null
    }

    private fun scanForUndefined(
        element: FormulaElement,
        declaredVars: List<String>,
        declaredLists: List<String>
    ): AnalysisResult? {

        if (element.type == FormulaElement.ElementType.USER_VARIABLE) {
            val varName = element.value?.toString() ?: ""
            if (varName.isNotBlank() && varName !in declaredVars) {
                return AnalysisResult(
                    Severity.ERROR,
                    context.getString(R.string.analysis_error_undefined_variable, varName)
                )
            }
        }

        if (element.type == FormulaElement.ElementType.USER_LIST) {
            val listName = element.value?.toString() ?: ""
            if (listName.isNotBlank() && listName !in declaredLists) {
                return AnalysisResult(
                    Severity.ERROR,
                    context.getString(R.string.analysis_error_undefined_list, listName)
                )
            }
        }

        element.leftChild?.let { scanForUndefined(it, declaredVars, declaredLists)?.let { return it } }
        element.rightChild?.let { scanForUndefined(it, declaredVars, declaredLists)?.let { return it } }
        for (child in element.additionalChildren) {
            scanForUndefined(child, declaredVars, declaredLists)?.let { return it }
        }

        return null
    }
}
