package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.content.bricks.*

class UnusedVariableRule(private val context2: Context) : AnalysisRule {
    override fun analyze(brick: Brick, context: GlobalAnalysisContext): AnalysisResult? {
        val varName = when (brick) {
            is SetVariableBrick -> brick.userVariable?.name
            is ChangeVariableBrick -> brick.userVariable?.name
            else -> null
        } ?: return null

        val isRead = context.variablesRead.contains(varName)

        if (!isRead) {
            return AnalysisResult(
                Severity.WARNING,
                context2.getString(R.string.analysis_unused_variable, varName)
            )
        }
        return null
    }
}
