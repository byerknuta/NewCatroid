package org.catrobat.catroid.codeanalysis

import org.catrobat.catroid.ProjectManager
import org.catrobat.catroid.content.bricks.*
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.formulaeditor.FormulaElement
import org.catrobat.catroid.formulaeditor.Operators

sealed class StaticValue {
    data class Number(val value: Double) : StaticValue()
    data class Bool(val value: Boolean) : StaticValue()
    data class Str(val value: String) : StaticValue()
    object Dynamic : StaticValue()
}

class GlobalAnalysisContext {
    val incomingStates = mutableMapOf<Brick, Map<String, StaticValue>>()
    var isPbrEnabledInProject: Boolean = false
    val allBroadcastsSent = mutableSetOf<String>()
    val variablesRead = mutableSetOf<String>()
    val allBroadcastsReceived = mutableSetOf<String>()

    companion object {
        fun build(): GlobalAnalysisContext {
            val context = GlobalAnalysisContext()
            val project = ProjectManager.getInstance().currentProject ?: return context

            for (scene in project.sceneList) {
                for (sprite in scene.spriteList) {
                    val allBricks = mutableListOf<Brick>()
                    for (script in sprite.scriptList) {
                        script.addToFlatList(allBricks)
                    }

                    for (brick in allBricks) {
                        if (CodeAnalyzer.isBrickCommented(brick)) continue

                        if (brick is EnablePbrRenderBrick) {
                            context.isPbrEnabledInProject = true
                        }
                        if (brick is BroadcastReceiverBrick) {
                            brick.broadcastMessage?.let { context.allBroadcastsReceived.add(it) }
                        }
                        if (brick is BroadcastBrick) {
                            brick.broadcastMessage?.let { context.allBroadcastsSent.add(it) }
                        }
                        if (brick is BroadcastWaitBrick) {
                            brick.broadcastMessage?.let { context.allBroadcastsSent.add(it) }
                        }

                        context.collectVariableReads(brick)
                    }
                }
            }

            for (scene in project.sceneList) {
                for (sprite in scene.spriteList) {
                    for (script in sprite.scriptList) {
                        context.propagateConstantsInScript(script)
                    }
                }
            }

            return context
        }
    }

    private fun collectVariableReads(brick: Brick) {
        if (brick is FormulaBrick) {
            for (formula in brick.allFormulaFieldsWithFormulas.values) {
                val root = formula.root ?: continue
                traverseAndCollectReads(root)
            }
        }
        try {
            val method = brick.javaClass.getMethod("getUserVariable")
            val userVar = method.invoke(brick) as? org.catrobat.catroid.formulaeditor.UserVariable
            if (userVar != null && brick !is SetVariableBrick) {
                variablesRead.add(userVar.name)
            }
        } catch (_: Exception) {}
    }

    private fun traverseAndCollectReads(element: FormulaElement, depth: Int = 0) {
        if (depth > 50) return
        val typeName = element.type.name
        if (typeName.contains("VAR")) {
            element.value?.toString()?.let { variablesRead.add(it) }
        }
        element.leftChild?.let { traverseAndCollectReads(it, depth + 1) }
        element.rightChild?.let { traverseAndCollectReads(it, depth + 1) }
        for (child in element.additionalChildren) {
            traverseAndCollectReads(child, depth + 1)
        }
    }

    private fun propagateConstantsInScript(script: org.catrobat.catroid.content.Script) {
        val initialState = emptyMap<String, StaticValue>()
        propagateInBrickList(script.brickList, initialState)
    }

    private fun propagateInBrickList(
        bricks: List<Brick>?,
        initialState: Map<String, StaticValue>,
        depth: Int = 0
    ): Map<String, StaticValue> {
        if (bricks == null || depth > 50) return initialState
        var currentState = initialState.toMap()

        for (brick in bricks) {
            incomingStates[brick] = currentState

            if (CodeAnalyzer.isBrickCommented(brick)) {
                continue
            }

            when (brick) {
                is SetVariableBrick -> {
                    val varName = brick.userVariable?.name
                    if (varName != null) {
                        val formula = brick.allFormulaFieldsWithFormulas.values.firstOrNull()
                        val value = if (formula != null) evaluateFormula(formula, currentState) else StaticValue.Dynamic
                        currentState = currentState + (varName to value)
                    }
                }
                is ChangeVariableBrick -> {
                    val varName = brick.userVariable?.name
                    if (varName != null) {
                        val currentVal = currentState[varName]
                        val formula = brick.allFormulaFieldsWithFormulas.values.firstOrNull()
                        if (currentVal is StaticValue.Number && formula != null) {
                            val changeVal = evaluateFormula(formula, currentState)
                            currentState = if (changeVal is StaticValue.Number) {
                                currentState + (varName to StaticValue.Number(currentVal.value + changeVal.value))
                            } else {
                                currentState + (varName to StaticValue.Dynamic)
                            }
                        } else {
                            currentState = currentState + (varName to StaticValue.Dynamic)
                        }
                    }
                }
                is CompositeBrick -> {
                    if (brick is IfLogicBeginBrick) {
                        val ifStateOut = propagateInBrickList(brick.nestedBricks, currentState, depth + 1)
                        val elseStateOut = if (brick.hasSecondaryList()) {
                            propagateInBrickList(brick.secondaryNestedBricks, currentState, depth + 1)
                        } else {
                            currentState
                        }
                        currentState = mergeStates(ifStateOut, elseStateOut)
                    } else {
                        val nestedFlat = mutableListOf<Brick>()
                        brick.addToFlatList(nestedFlat)
                        val writtenInLoop = nestedFlat.filterIsInstance<SetVariableBrick>().mapNotNull { it.userVariable?.name } +
                                nestedFlat.filterIsInstance<ChangeVariableBrick>().mapNotNull { it.userVariable?.name }

                        var loopStateIn = currentState.toMutableMap()
                        for (v in writtenInLoop) {
                            loopStateIn[v] = StaticValue.Dynamic
                        }

                        propagateInBrickList(brick.nestedBricks, loopStateIn, depth + 1)
                        if (brick.hasSecondaryList()) {
                            propagateInBrickList(brick.secondaryNestedBricks, loopStateIn, depth + 1)
                        }
                        if (brick is TryCatchFinallyBrick) {
                            propagateInBrickList(brick.thirdNestedBricks, loopStateIn, depth + 1)
                        }

                        currentState = loopStateIn
                    }
                }
            }
        }
        return currentState
    }

    private fun mergeStates(s1: Map<String, StaticValue>, s2: Map<String, StaticValue>): Map<String, StaticValue> {
        val merged = mutableMapOf<String, StaticValue>()
        val allKeys = s1.keys + s2.keys
        for (k in allKeys) {
            val v1 = s1[k] ?: StaticValue.Dynamic
            val v2 = s2[k] ?: StaticValue.Dynamic
            if (v1 == v2) {
                merged[k] = v1
            } else {
                merged[k] = StaticValue.Dynamic
            }
        }
        return merged
    }

    fun evaluateFormula(formula: Formula, state: Map<String, StaticValue>): StaticValue {
        val root = formula.root ?: return StaticValue.Dynamic
        return evaluateElement(root, state)
    }

    private fun evaluateElement(element: FormulaElement, state: Map<String, StaticValue>, depth: Int = 0): StaticValue {
        if (depth > 50) return StaticValue.Dynamic

        val type = element.type
        val valueStr = element.value?.toString() ?: ""

        when (type) {
            FormulaElement.ElementType.NUMBER -> {
                val d = valueStr.toDoubleOrNull() ?: return StaticValue.Dynamic
                return StaticValue.Number(d)
            }
            FormulaElement.ElementType.STRING -> {
                return StaticValue.Str(valueStr)
            }
            FormulaElement.ElementType.USER_VARIABLE -> {
                return state[valueStr] ?: StaticValue.Dynamic
            }
            FormulaElement.ElementType.OPERATOR -> {
                val v: Any? = element.value
                val opStr = when (v) {
                    is Operators -> v.name
                    is Enum<*> -> v.name
                    else -> v?.toString()?.uppercase() ?: ""
                }

                if (opStr == "LOGICAL_NOT") {
                    val child = element.leftChild?.let { evaluateElement(it, state, depth + 1) }
                        ?: element.rightChild?.let { evaluateElement(it, state, depth + 1) }
                    return when (child) {
                        is StaticValue.Bool -> StaticValue.Bool(!child.value)
                        else -> StaticValue.Dynamic
                    }
                }

                val left = element.leftChild?.let { evaluateElement(it, state, depth + 1) } ?: StaticValue.Dynamic
                val right = element.rightChild?.let { evaluateElement(it, state, depth + 1) } ?: StaticValue.Dynamic

                if (opStr == "MINUS" && element.leftChild == null) {
                    return when (right) {
                        is StaticValue.Number -> StaticValue.Number(-right.value)
                        else -> StaticValue.Dynamic
                    }
                }

                if (left is StaticValue.Number && right is StaticValue.Number) {
                    val l = left.value
                    val r = right.value
                    return when (opStr) {
                        "PLUS" -> StaticValue.Number(l + r)
                        "MINUS" -> StaticValue.Number(l - r)
                        "MULT" -> StaticValue.Number(l * r)
                        "DIVIDE" -> if (r == 0.0) StaticValue.Dynamic else StaticValue.Number(l / r)
                        "MOD" -> if (r == 0.0) StaticValue.Dynamic else StaticValue.Number(l % r)
                        "POW" -> {
                            val powRes = Math.pow(l, r)
                            if (powRes.isNaN() || powRes.isInfinite()) StaticValue.Dynamic else StaticValue.Number(powRes)
                        }
                        "EQUAL" -> StaticValue.Bool(l == r)
                        "NOT_EQUAL" -> StaticValue.Bool(l != r)
                        "SMALLER_THAN" -> StaticValue.Bool(l < r)
                        "GREATER_THAN" -> StaticValue.Bool(l > r)
                        "SMALLER_OR_EQUAL" -> StaticValue.Bool(l <= r)
                        "GREATER_OR_EQUAL" -> StaticValue.Bool(l >= r)
                        else -> StaticValue.Dynamic
                    }
                }

                if (left is StaticValue.Bool && right is StaticValue.Bool) {
                    val l = left.value
                    val r = right.value
                    return when (opStr) {
                        "LOGICAL_AND" -> StaticValue.Bool(l && r)
                        "LOGICAL_OR" -> StaticValue.Bool(l || r)
                        "EQUAL" -> StaticValue.Bool(l == r)
                        "NOT_EQUAL" -> StaticValue.Bool(l != r)
                        else -> StaticValue.Dynamic
                    }
                }
                return StaticValue.Dynamic
            }
            FormulaElement.ElementType.FUNCTION -> {
                val funcName = valueStr.uppercase()
                val left = element.leftChild?.let { evaluateElement(it, state, depth + 1) }

                if (left is StaticValue.Number) {
                    val v = left.value
                    val res = when (funcName) {
                        "SIN" -> Math.sin(Math.toRadians(v))
                        "COS" -> Math.cos(Math.toRadians(v))
                        "TAN" -> Math.tan(Math.toRadians(v))
                        "LN" -> Math.log(v)
                        "LOG" -> Math.log10(v)
                        "SQRT" -> Math.sqrt(v)
                        "ABS" -> Math.abs(v)
                        "ROUND" -> Math.round(v).toDouble()
                        "CEIL" -> Math.ceil(v)
                        "FLOOR" -> Math.floor(v)
                        "EXP" -> Math.exp(v)
                        else -> null
                    }
                    if (res != null && !res.isNaN() && !res.isInfinite()) {
                        return StaticValue.Number(res)
                    }
                }

                if (element.leftChild != null && element.rightChild != null) {
                    val leftVal = evaluateElement(element.leftChild, state, depth + 1)
                    val rightVal = evaluateElement(element.rightChild, state, depth + 1)
                    if (leftVal is StaticValue.Number && rightVal is StaticValue.Number) {
                        val l = leftVal.value
                        val r = rightVal.value
                        val res = when (funcName) {
                            "MIN" -> Math.min(l, r)
                            "MAX" -> Math.max(l, r)
                            "POW" -> Math.pow(l, r)
                            else -> null
                        }
                        if (res != null) return StaticValue.Number(res)
                    }
                }
                return StaticValue.Dynamic
            }
            else -> return StaticValue.Dynamic
        }
    }
}
