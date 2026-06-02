package org.catrobat.catroid.codeanalysis

import org.catrobat.catroid.content.bricks.Brick

object AnalysisManager {
    private var analysisResults: Map<Brick, AnalysisResult> = emptyMap()

    fun updateResults(results: Map<Brick, AnalysisResult>) {
        this.analysisResults = results
    }

    fun getResultFor(brick: Brick): AnalysisResult? {
        return analysisResults[brick]
    }

    fun getResults(): Map<Brick, AnalysisResult> {
        return analysisResults
    }

    fun getErrorCount(): Int {
        return analysisResults.values.count { it.severity == Severity.ERROR }
    }

    fun getWarningCount(): Int {
        return analysisResults.values.count { it.severity == Severity.WARNING }
    }

    fun clearResults() {
        analysisResults = emptyMap()
    }
}
