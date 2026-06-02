package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.content.bricks.*
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.formulaeditor.FormulaElement
import org.catrobat.catroid.formulaeditor.UserData

class RedundantBlockRule(private val context: Context) : AnalysisRule {
    override fun analyze(brick: Brick): AnalysisResult? {
        if (brick is CompositeBrick || brick is NoteBrick) {
            return null
        }

        val parent = brick.parent ?: return null
        val siblingBricks = parent.dragAndDropTargetList ?: return null
        val brickIndex = siblingBricks.indexOf(brick)

        if (brickIndex < siblingBricks.size - 1) {
            if (isBrickActionOverwrittenBeforeUse(brick, brickIndex + 1, siblingBricks)) {
                return AnalysisResult(
                    Severity.WARNING,
                    context.getString(R.string.analysis_redundant_overwritten)
                )
            }
        }

        if (brickIndex > 0) {
            val previousBrick = siblingBricks[brickIndex - 1]
            if (brick.javaClass == previousBrick.javaClass && areBricksEffectivelyEqual(brick, previousBrick)) {
                return AnalysisResult(
                    Severity.WARNING,
                    context.getString(R.string.analysis_redundant_same_action)
                )
            }
        }
        return null
    }

    private fun isBrickActionOverwrittenBeforeUse(brick: Brick, startIndex: Int, siblings: List<Brick>): Boolean {
        if (brick is SetVariableBrick) {
            val varName = brick.userVariable?.name ?: return false
            return isVariableOverwrittenBeforeUse(varName, startIndex, siblings)
        }

        if (brick is SetXBrick) {
            return isPositionXOverwrittenBeforeUse(startIndex, siblings)
        }

        if (brick is SetYBrick) {
            return isPositionYOverwrittenBeforeUse(startIndex, siblings)
        }

        if (brick is PlaceAtBrick) {
            return isPositionXOverwrittenBeforeUse(startIndex, siblings) &&
                    isPositionYOverwrittenBeforeUse(startIndex, siblings)
        }

        if (brick is SetLookBrick) {
            return isLookOverwrittenBeforeUse(startIndex, siblings)
        }

        return false
    }

    private fun isVariableOverwrittenBeforeUse(varName: String, startIndex: Int, siblings: List<Brick>): Boolean {
        for (i in startIndex until siblings.size) {
            val sibling = siblings[i]

            if (usesVariable(sibling, varName)) {
                return false
            }

            if (sibling is SetVariableBrick && sibling.userVariable?.name == varName) {
                return true
            }

            if (sibling is ChangeVariableBrick && sibling.userVariable?.name == varName) {
                return false
            }

            if (sibling is CompositeBrick) {
                val nestedFlatList = mutableListOf<Brick>()
                sibling.addToFlatList(nestedFlatList)
                if (nestedFlatList.any { usesVariable(it, varName) }) {
                    return false
                }
            }

            if (sibling is StopScriptBrick && isHaltingStopScript(sibling)) {
                return true
            }
        }
        return false
    }

    private fun isPositionXOverwrittenBeforeUse(startIndex: Int, siblings: List<Brick>): Boolean {
        for (i in startIndex until siblings.size) {
            val sibling = siblings[i]

            if (sibling is WaitBrick || sibling is GlideToBrick || sibling is ChangeXByNBrick || sibling is MoveNStepsBrick) {
                return false
            }
            if (sibling is SetXBrick || sibling is PlaceAtBrick) {
                return true
            }
        }
        return false
    }

    private fun isPositionYOverwrittenBeforeUse(startIndex: Int, siblings: List<Brick>): Boolean {
        for (i in startIndex until siblings.size) {
            val sibling = siblings[i]
            if (sibling is WaitBrick || sibling is GlideToBrick || sibling is ChangeYByNBrick || sibling is MoveNStepsBrick) {
                return false
            }
            if (sibling is SetYBrick || sibling is PlaceAtBrick) {
                return true
            }
        }
        return false
    }

    private fun isLookOverwrittenBeforeUse(startIndex: Int, siblings: List<Brick>): Boolean {
        for (i in startIndex until siblings.size) {
            val sibling = siblings[i]
            if (sibling is WaitBrick || sibling is NextLookBrick || sibling is PreviousLookBrick) {
                return false
            }
            if (sibling is SetLookBrick) {
                return true
            }
        }
        return false
    }

    private fun usesVariable(brick: Brick, varName: String): Boolean {
        if (brick is FormulaBrick) {
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

    private fun isHaltingStopScript(brick: StopScriptBrick): Boolean {
        try {
            val field = brick.javaClass.getDeclaredField("selection")
            field.isAccessible = true
            val value = field.get(brick)?.toString() ?: ""
            return !value.contains("other") && !value.contains("другие")
        } catch (e: Exception) {
            try {
                val field = brick.javaClass.getDeclaredField("stopType")
                field.isAccessible = true
                val value = field.get(brick)?.toString() ?: ""
                return !value.contains("other") && !value.contains("другие")
            } catch (e2: Exception) {
                return true
            }
        }
    }

    private fun areBricksEffectivelyEqual(brick1: Brick, brick2: Brick): Boolean {
        if (brick1 is NoteBrick || brick2 is NoteBrick) return false

        if (brick1 is FormulaBrick && brick2 is FormulaBrick) {
            val formulas1 = brick1.allFormulaFieldsWithFormulas
            val formulas2 = brick2.allFormulaFieldsWithFormulas
            if (formulas1.size != formulas2.size) return false
            if (formulas1.isEmpty()) return true
            return formulas1.all { (field, formula1) ->
                val formula2 = formulas2[field]
                formula2 != null && formula1.getTrimmedFormulaString(context) == formula2.getTrimmedFormulaString(context)
            }
        }

        if (brick1 is UserDataBrick && brick2 is UserDataBrick) {
            val data1 = brick1.allBrickDataWithValues
            val data2 = brick2.allBrickDataWithValues
            if (data1.size != data2.size) return false
            if (data1.isEmpty()) return true
            return data1.all { (field, value1) ->
                val value2 = data2[field]
                val name1 = (value1 as? UserData)?.name
                val name2 = (value2 as? UserData)?.name
                (name1 == null && name2 == null) || (name1 != null && name1 == name2)
            }
        }

        if (brick1 is SetLookBrick && brick2 is SetLookBrick) {
            return brick1.look?.name == brick2.look?.name
        }

        return false
    }
}
