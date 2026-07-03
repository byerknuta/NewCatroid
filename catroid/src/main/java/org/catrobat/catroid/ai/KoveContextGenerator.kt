package org.catrobat.catroid.ai

import org.catrobat.catroid.content.Sprite
import org.catrobat.catroid.content.Script
import org.catrobat.catroid.content.bricks.Brick
import org.catrobat.catroid.content.bricks.CompositeBrick
import org.catrobat.catroid.content.bricks.IfThenLogicBeginBrick
import org.catrobat.catroid.content.bricks.FormulaBrick
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.formulaeditor.FormulaElement
import org.catrobat.catroid.common.LookData
import org.catrobat.catroid.common.SoundInfo
import java.lang.reflect.Modifier

object KoveContextGenerator {

    fun generateContext(sprite: Sprite, activeScript: Script, cursorBrickIndex: Int): Pair<String, String> {
        val prefixBuilder = StringBuilder()
        val suffixBuilder = StringBuilder()

        val looksList = sprite.lookList.map { "'${it.name}'" }
        val soundsList = sprite.soundList.map { "'${it.name}'" }

        prefixBuilder.append("@Sprite(\"${sprite.name}\")\n")
        prefixBuilder.append("class ${toCamelCase(sprite.name)}:\n")
        prefixBuilder.append("    looks = $looksList\n")
        prefixBuilder.append("    sounds = $soundsList\n\n")

        for (script in sprite.scriptList) {
            val scriptName = script.javaClass.simpleName
            val isEditing = (script == activeScript)
            val decorator = "@$scriptName"
            val defName = toSnakeCase(scriptName.replace("Script", ""))

            if (isEditing) {
                prefixBuilder.append("    $decorator\n")
                prefixBuilder.append("    def $defName():\n")

                val safeCursorIndex = if (cursorBrickIndex < 0) 0 else cursorBrickIndex
                val bricksBefore = script.brickList.take(safeCursorIndex)
                val bricksAfter = script.brickList.drop(safeCursorIndex)

                // Если перед курсором пусто — обязательно пишем pass, чтобы не ломать синтаксис Python!
                if (bricksBefore.isEmpty()) {
                    prefixBuilder.append("        pass\n")
                } else {
                    serializeBricks(bricksBefore, prefixBuilder, indent = 8)
                }

                serializeBricks(bricksAfter, suffixBuilder, indent = 8)
            } else {
                prefixBuilder.append("    $decorator\n")
                prefixBuilder.append("    def $defName():\n")

                if (script.brickList.isEmpty()) {
                    prefixBuilder.append("        pass\n")
                } else {
                    serializeBricks(script.brickList, prefixBuilder, indent = 8)
                }
                prefixBuilder.append("\n")
            }
        }

        return Pair(prefixBuilder.toString(), suffixBuilder.toString())
    }

    private fun serializeBricks(bricks: List<Brick>, builder: StringBuilder, indent: Int) {
        val spaces = " ".repeat(indent)
        for (brick in bricks) {
            if (brick.isCommentedOut) continue
            val cleanName = cleanBrickName(brick.javaClass.simpleName)

            if (brick is CompositeBrick) {
                if (brick is IfThenLogicBeginBrick) {
                    val condFormula = getFormulaFromBrick(brick, Brick.BrickField.IF_CONDITION)
                    val condStr = serializeFormula(condFormula?.formulaTree)
                    builder.append(spaces).append("if $condStr:\n")
                    serializeBricks(brick.nestedBricks, builder, indent + 4)

                    if (brick.hasSecondaryList() && brick.secondaryNestedBricks != null) {
                        builder.append(spaces).append("else:\n")
                        serializeBricks(brick.secondaryNestedBricks, builder, indent + 4)
                    }
                } else if (cleanName == "forever") {
                    builder.append(spaces).append("forever():\n")
                    serializeBricks(brick.nestedBricks, builder, indent + 4)
                } else if (cleanName == "repeat") {
                    val timesFormula = getFormulaFromBrick(brick, Brick.BrickField.TIMES_TO_REPEAT)
                    val timesStr = serializeFormula(timesFormula?.formulaTree)
                    builder.append(spaces).append("repeat($timesStr):\n")
                    serializeBricks(brick.nestedBricks, builder, indent + 4)
                } else if (cleanName == "repeat_until") {
                    val condFormula = getFormulaFromBrick(brick, Brick.BrickField.REPEAT_UNTIL_CONDITION)
                    val condStr = serializeFormula(condFormula?.formulaTree)
                    builder.append(spaces).append("repeat_until($condStr):\n")
                    serializeBricks(brick.nestedBricks, builder, indent + 4)
                } else {
                    builder.append(spaces).append("$cleanName():\n")
                    serializeBricks(brick.nestedBricks, builder, indent + 4)
                }
            } else {
                val formulas = getAllFormulasFromBrick(brick)
                val primitives = getPrimitiveFields(brick)
                val totalArgsCount = formulas.size + primitives.size

                if (totalArgsCount == 1) {
                    // ЕСЛИ ОДИН АРГУМЕНТ — ГЕНЕРИРУЕМ БЕЗ ИМЕНИ ( wait(0.2) )
                    val valOnly = if (formulas.isNotEmpty()) {
                        serializeFormula(formulas.values.first().formulaTree)
                    } else {
                        val value = primitives.values.first()
                        if (value is String) "\"$value\"" else value.toString()
                    }
                    builder.append(spaces).append("$cleanName($valOnly)\n")
                } else {
                    // ЕСЛИ МНОГО — ГЕНЕРИРУЕМ С ИМЕНАМИ
                    val args = mutableListOf<String>()
                    formulas.forEach { (field, formula) ->
                        val paramName = getShortParamName(field.name)
                        args.add("$paramName=${serializeFormula(formula.formulaTree)}")
                    }
                    primitives.forEach { (name, value) ->
                        val paramName = toSnakeCase(name)
                        val valStr = when (value) {
                            is String -> "\"$value\""
                            is Boolean -> value.toString().replaceFirstChar { it.uppercase() }
                            else -> value.toString()
                        }
                        args.add("$paramName=$valStr")
                    }
                    builder.append(spaces).append("$cleanName(${args.joinToString(", ")})\n")
                }
            }
        }
    }

    fun serializeFormula(element: FormulaElement?): String {
        if (element == null) return ""
        return when (element.elementType) {
            FormulaElement.ElementType.NUMBER -> element.value ?: "0"
            FormulaElement.ElementType.STRING -> "\"${element.value}\""
            FormulaElement.ElementType.USER_VARIABLE -> "var(\"${element.value}\")"
            FormulaElement.ElementType.USER_LIST -> "list(\"${element.value}\")"
            FormulaElement.ElementType.SENSOR -> "sensor(\"${element.value}\")"
            FormulaElement.ElementType.COLLISION_FORMULA -> "touches(\"${element.value}\")"
            FormulaElement.ElementType.USER_DEFINED_BRICK_INPUT -> "input(\"${element.value}\")"
            FormulaElement.ElementType.OPERATOR -> {
                val op = mapOperatorToSymbol(element.value)
                val left = serializeFormula(element.leftChild)
                val right = serializeFormula(element.rightChild)
                if (left.isNotEmpty()) "($left $op $right)" else "($op$right)"
            }
            FormulaElement.ElementType.FUNCTION -> {
                val funcName = element.value.lowercase()
                val args = mutableListOf<String>()
                if (element.leftChild != null) args.add(serializeFormula(element.leftChild))
                if (element.rightChild != null) args.add(serializeFormula(element.rightChild))
                element.additionalChildren?.forEach { args.add(serializeFormula(it)) }
                "$funcName(${args.joinToString(", ")})"
            }
            else -> element.value ?: ""
        }
    }

    private fun getFormulaFromBrick(brick: Brick, field: Brick.BrickField): Formula? {
        if (brick is FormulaBrick) {
            return brick.allFormulaFieldsWithFormulas?.get(field)
        }
        return null
    }

    private fun getAllFormulasFromBrick(brick: Brick): Map<Brick.BrickField, Formula> {
        val result = mutableMapOf<Brick.BrickField, Formula>()
        if (brick is FormulaBrick) {
            val formulas = brick.allFormulaFieldsWithFormulas
            if (formulas != null) {
                result.putAll(formulas)
            }
        }
        return result
    }

    private fun getPrimitiveFields(brick: Brick): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        var clazz: Class<*>? = brick.javaClass
        val ignoredFields = setOf(
            "serialVersionUID", "view", "checkbox", "spinner", "parent", "endBrick", "loopBricks",
            "tryBricks", "catchBricks", "finallyBricks", "posX", "posY", "scriptId", "scriptBrick", "commentedOut"
        )
        while (clazz != null && clazz.name.startsWith("org.catrobat.catroid")) {
            for (field in clazz.declaredFields) {
                if (Modifier.isTransient(field.modifiers)) continue
                if (ignoredFields.contains(field.name)) continue

                val type = field.type
                if (type.isPrimitive || type == String::class.java || type.isEnum) {
                    field.isAccessible = true
                    val value = field.get(brick)
                    if (value != null) result[field.name] = value
                } else if (LookData::class.java.isAssignableFrom(type) || SoundInfo::class.java.isAssignableFrom(type)) {
                    // Поддержка выгрузки имен образов и звуков!
                    field.isAccessible = true
                    val value = field.get(brick)
                    if (value != null) {
                        try {
                            val nameMethod = value.javaClass.getMethod("getName")
                            val name = nameMethod.invoke(value) as String
                            result[field.name] = name
                        } catch (e: Exception) {}
                    }
                }
            }
            clazz = clazz.superclass
        }
        return result
    }

    private fun mapOperatorToSymbol(op: String): String {
        val map = mapOf(
            "PLUS" to "+", "MINUS" to "-", "MULT" to "*", "DIVIDE" to "/",
            "POW" to "**", "MOD" to "%", "EQUAL" to "==", "NOT_EQUAL" to "!=",
            "GREATER_THAN" to ">", "GREATER_OR_EQUAL" to ">=", "SMALLER_THAN" to "<",
            "SMALLER_OR_EQUAL" to "<=", "LOGICAL_AND" to "and", "LOGICAL_OR" to "or", "LOGICAL_NOT" to "not"
        )
        return map[op] ?: op
    }

    private fun cleanBrickName(className: String): String {
        var name = className
        if (name.endsWith("Brick")) name = name.substring(0, name.length - 5)
        name = toSnakeCase(name)
        val renameMap = mapOf(
            "set_size_to" to "set_size", "set_variable" to "set_var",
            "change_variable" to "change_var", "place_at" to "place_at",
            "play_sound_and_wait" to "play_sound_wait", "scene_start" to "start_scene"
        )
        return renameMap[name] ?: name
    }

    private fun getShortParamName(fieldName: String): String {
        val categoryMap = mapOf(
            "VALUE" to "val", "VALUE_1" to "val1", "VALUE_2" to "val2", "VALUE_3" to "val3",
            "TIMES_TO_REPEAT" to "times", "INTERVAL" to "interval", "X_POSITION" to "x", "Y_POSITION" to "y",
            "SIZE" to "size", "COLOR" to "color", "STEPS" to "steps", "DEGREES" to "degrees",
            "IF_CONDITION" to "cond", "REPEAT_UNTIL_CONDITION" to "cond", "VARIABLE_CHANGE" to "val",
            "VARIABLE" to "val", "OPEN_URL" to "url", "OPEN_URL_STRING" to "url", "DURATION" to "duration", "VOLUME" to "volume"
        )
        return categoryMap[fieldName.uppercase()] ?: toSnakeCase(fieldName)
    }

    private fun toSnakeCase(s: String): String {
        return s.replace(Regex("([a-z0-9])([A-Z])"), "$1_$2").lowercase()
    }

    private fun toCamelCase(s: String): String {
        return s.split("_").joinToString("") { part ->
            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
