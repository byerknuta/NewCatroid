package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.content.Script
import org.catrobat.catroid.content.bricks.Brick
import org.catrobat.catroid.content.bricks.CompositeBrick
import org.catrobat.catroid.content.bricks.TryCatchFinallyBrick

class CodeAnalyzer(private val context: Context) {
    private val rules = listOf<AnalysisRule>(
        ErrorRule(context),
        EmptyLoopRule(context),
        ConstantConditionRule(context),
        UnreachableCodeRule(context),
        RedundantBlockRule(context),
        ResourceLeakRule(context),
        InvalidCloneUsageRule(context),
        ParameterValidationRule(context),
        UndefinedReferenceRule(context),
        UnusedVariableRule(context),
        DeadScriptRule(context),
        ThreedCompatibilityRule(context),
        EventCascadeRule(context),
        DivisionByZeroRule(context)
    )

    fun analyzeScript(script: Script): Map<Brick, AnalysisResult> {
        val results = mutableMapOf<Brick, AnalysisResult>()
        try {
            val globalContext = GlobalAnalysisContext.build()

            analyzeBrickList(script.brickList, globalContext, results, isParentCommented = false)

            val allBricks = mutableListOf<Brick>()
            script.addToFlatList(allBricks)
            val commentedBricks = allBricks.filter { isBrickCommentedDirectly(it) }

            val threshold = 100
            if (commentedBricks.size >= threshold) {
                val firstCommented = commentedBricks.firstOrNull()
                if (firstCommented != null) {
                    results[firstCommented] = AnalysisResult(
                        Severity.WARNING,
                        context.getString(R.string.analysis_too_many_commented_bricks, commentedBricks.size)
                    )
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return results
    }

    private fun analyzeBrickList(
        brickList: List<Brick>,
        globalContext: GlobalAnalysisContext,
        results: MutableMap<Brick, AnalysisResult>,
        isParentCommented: Boolean
    ) {
        for (brick in brickList) {
            val isCommented = isParentCommented || isBrickCommentedDirectly(brick)

            if (isCommented) {
                traverseNestedBricks(brick, globalContext, results, isParentCommented = true)
                continue
            }

            for (rule in rules) {
                val result = rule.analyze(brick, globalContext)
                if (result != null) {
                    results[brick] = result
                    break
                }
            }

            traverseNestedBricks(brick, globalContext, results, isParentCommented = false)
        }
    }

    private fun traverseNestedBricks(
        brick: Brick,
        globalContext: GlobalAnalysisContext,
        results: MutableMap<Brick, AnalysisResult>,
        isParentCommented: Boolean
    ) {
        if (brick is CompositeBrick) {
            brick.nestedBricks?.let { analyzeBrickList(it, globalContext, results, isParentCommented) }

            if (brick.hasSecondaryList()) {
                brick.secondaryNestedBricks?.let { analyzeBrickList(it, globalContext, results, isParentCommented) }
            }

            if (brick is TryCatchFinallyBrick) {
                brick.thirdNestedBricks?.let { analyzeBrickList(it, globalContext, results, isParentCommented) }
            }
        }
    }

    companion object {
        fun isBrickCommentedDirectly(brick: Brick?): Boolean {
            if (brick == null) return false
            return try {
                val method = brick.javaClass.getMethod("isCommentedOut")
                method.invoke(brick) as? Boolean ?: false
            } catch (_: Exception) {
                try {
                    val field = brick.javaClass.getField("commentedOut")
                    field.isAccessible = true
                    field.get(brick) as? Boolean ?: false
                } catch (_: Exception) {
                    false
                }
            }
        }

        fun isBrickCommented(brick: Brick?): Boolean {
            val visited = mutableSetOf<Any>()
            var current: Any? = brick

            while (current != null && visited.add(current)) {
                if (current is Brick) {
                    if (isBrickCommentedDirectly(current)) return true

                    val parent = try {
                        val parentField = current.javaClass.getField("parent")
                        parentField.isAccessible = true
                        parentField.get(current)
                    } catch (_: Exception) {
                        try {
                            val parentMethod = current.javaClass.getMethod("getParent")
                            parentMethod.invoke(current)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    current = parent
                } else {
                    break
                }
            }
            return false
        }
    }
}
