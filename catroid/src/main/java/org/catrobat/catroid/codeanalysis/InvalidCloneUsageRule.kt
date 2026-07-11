package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.content.WhenClonedScript
import org.catrobat.catroid.content.bricks.Brick
import org.catrobat.catroid.content.bricks.DeleteThisCloneBrick

class InvalidCloneUsageRule(private val context: Context) : AnalysisRule {
    override fun analyze(brick: Brick, context2: GlobalAnalysisContext): AnalysisResult? {
        if (brick is DeleteThisCloneBrick) {
            val parentScript = brick.script
            if (parentScript != null && parentScript !is WhenClonedScript) {
                return AnalysisResult(Severity.WARNING, context.getString(R.string.analysis_clone_delete_outside_cloned))
            }
        }
        return null
    }
}
