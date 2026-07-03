package org.catrobat.catroid.ai

import org.catrobat.catroid.content.Sprite
import org.catrobat.catroid.content.bricks.Brick
import org.catrobat.catroid.content.bricks.CompositeBrick
import org.catrobat.catroid.content.bricks.FormulaBrick
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.formulaeditor.FormulaElement
import org.catrobat.catroid.common.LookData
import org.catrobat.catroid.common.SoundInfo
import java.lang.reflect.Modifier

// =========================================================================
// 1. AST-СТРУКТУРЫ ДАННЫХ
// =========================================================================

enum class FormulaElementType {
    OPERATOR, FUNCTION, NUMBER, SENSOR, USER_VARIABLE, USER_LIST, USER_DEFINED_BRICK_INPUT, BRACKET, STRING, COLLISION_FORMULA
}

data class ParsedFormulaElement(
    val type: FormulaElementType,
    val value: String,
    val leftChild: ParsedFormulaElement? = null,
    val rightChild: ParsedFormulaElement? = null,
    val additionalChildren: List<ParsedFormulaElement> = emptyList()
)

sealed class ParsedBrick {
    data class Simple(
        val name: String,
        val arguments: Map<String, ParsedFormulaElement>
    ) : ParsedBrick()

    data class If(
        val condition: ParsedFormulaElement,
        val thenBranch: List<ParsedBrick>,
        val elseBranch: List<ParsedBrick>?
    ) : ParsedBrick()

    data class Loop(
        val loopType: String,
        val arguments: Map<String, ParsedFormulaElement>,
        val body: List<ParsedBrick>
    ) : ParsedBrick()
}

// =========================================================================
// 2. АВТОНОМНЫЙ КОНВЕРТЕР
// =========================================================================

object KoveModelConverter {

    private val shortToBrickFieldMap: Map<String, Brick.BrickField> by lazy {
        val map = mutableMapOf<String, Brick.BrickField>()
        for (field in Brick.BrickField.values()) {
            val shortName = getShortParamName(field.name)
            map[shortName] = field
        }
        map
    }

    fun buildBricks(parsedList: List<ParsedBrick>, sprite: Sprite): List<Brick> {
        val result = mutableListOf<Brick>()
        for (pb in parsedList) {
            val brick = convertSingleBrick(pb, sprite)
            if (brick != null) {
                result.add(brick)
            }
        }
        return result
    }

    fun convertSingleBrick(parsed: ParsedBrick, sprite: Sprite): Brick? {
        return when (parsed) {
            is ParsedBrick.Simple -> {
                val brick = instantiateBrickByName(parsed.name)
                if (brick != null) {
                    populateBrickArguments(brick, parsed.arguments, sprite)
                }
                brick
            }
            is ParsedBrick.If -> {
                val targetName = if (parsed.elseBranch != null) "if_logic_begin" else "if_then_logic_begin"
                val ifBrick = instantiateBrickByName(targetName)
                if (ifBrick != null && ifBrick is CompositeBrick) {
                    val condFormula = buildFormula(parsed.condition)
                    setFormulaOnBrick(ifBrick, Brick.BrickField.IF_CONDITION, condFormula)

                    val thenBranch = buildBricks(parsed.thenBranch, sprite)
                    addNestedBricksToComposite(ifBrick, thenBranch, secondary = false)

                    if (parsed.elseBranch != null && ifBrick.hasSecondaryList()) {
                        val elseBranch = buildBricks(parsed.elseBranch, sprite)
                        addNestedBricksToComposite(ifBrick, elseBranch, secondary = true)
                    }
                }
                ifBrick
            }
            is ParsedBrick.Loop -> {
                val loopBrick = instantiateBrickByName(parsed.loopType)
                if (loopBrick != null && loopBrick is CompositeBrick) {
                    populateBrickArguments(loopBrick, parsed.arguments, sprite)

                    val body = buildBricks(parsed.body, sprite)
                    addNestedBricksToComposite(loopBrick, body, secondary = false)
                }
                loopBrick
            }
        }
    }

    fun convertFormula(parsed: ParsedFormulaElement): Formula {
        return buildFormula(parsed)
    }

    private fun buildFormula(parsed: ParsedFormulaElement, parent: FormulaElement? = null): Formula {
        val rootElement = buildFormulaElement(parsed, parent)
        return Formula(rootElement)
    }

    private fun buildFormulaElement(parsed: ParsedFormulaElement, parent: FormulaElement? = null): FormulaElement {
        val type = FormulaElement.ElementType.valueOf(parsed.type.name)
        val element = FormulaElement(type, parsed.value, parent)

        // ВАЖНО: Принудительный вызов Java-сеттеров setLeftChild/setRightChild
        // вместо прямой перезаписи полей, чтобы восстановить связь parent!
        parsed.leftChild?.let {
            val child = buildFormulaElement(it, element)
            element.setLeftChild(child)
        }
        parsed.rightChild?.let {
            val child = buildFormulaElement(it, element)
            element.setRightChild(child)
        }
        parsed.additionalChildren.forEach {
            val child = buildFormulaElement(it, element)
            element.addAdditionalChild(child)
        }

        return element
    }

    private fun instantiateBrickByName(dslName: String): Brick? {
        val camelCaseName = dslName.split("_").joinToString("") { part ->
            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        val possibleClassNames = listOf(
            "org.catrobat.catroid.content.bricks.${camelCaseName}Brick",
            "org.catrobat.catroid.content.bricks.${camelCaseName}"
        )

        for (className in possibleClassNames) {
            try {
                val clazz = Class.forName(className)
                val constructor = clazz.getDeclaredConstructor()
                constructor.isAccessible = true
                return constructor.newInstance() as Brick
            } catch (ignored: Exception) {}
        }
        return null
    }

    private fun populateBrickArguments(brick: Brick, arguments: Map<String, ParsedFormulaElement>, sprite: Sprite) {
        for ((argName, parsedFormula) in arguments) {
            var targetArgName = argName

            // ВОССТАНОВЛЕНИЕ ОДИНОЧНОГО ПАРАМЕТРА ( wait(0.2) -> wait(time=0.2) )
            if (targetArgName == "val") {
                // Если у FormulaBrick ровно 1 разрешенное поле — используем его
                val allowedFields = getAllFormulasFromBrick(brick).keys
                if (allowedFields.size == 1) {
                    val formula = buildFormula(parsedFormula)
                    setFormulaOnBrick(brick, allowedFields.first(), formula)
                    continue
                }

                // Фолбек: если это не FormulaBrick, проверяем единственное примитивное поле
                val primitives = getPrimitiveFields(brick)
                if (primitives.size == 1) {
                    targetArgName = primitives.keys.first()
                }
            }

            val matchedBrickField = shortToBrickFieldMap[targetArgName]
            if (matchedBrickField != null) {
                val formula = buildFormula(parsedFormula)
                val success = setFormulaOnBrick(brick, matchedBrickField, formula)
                if (success) continue
            }

            val camelFieldName = toCamelCaseField(targetArgName)
            val constantValue = extractConstantFromFormula(parsedFormula)
            if (constantValue != null) {
                setPrimitiveFieldOnBrick(brick, camelFieldName, constantValue, sprite)
            }
        }
    }

    private fun setFormulaOnBrick(brick: Brick, field: Brick.BrickField, formula: Formula): Boolean {
        var clazz: Class<*>? = brick.javaClass
        while (clazz != null && clazz.name.startsWith("org.catrobat.catroid")) {
            try {
                val method = clazz.getDeclaredMethod("setFormulaWithBrickField", Brick.BrickField::class.java, Formula::class.java)
                method.isAccessible = true
                method.invoke(brick, field, formula)
                return true
            } catch (ignored: NoSuchMethodException) {}
            clazz = clazz.superclass
        }

        val setterName = "set" + toCamelCase(field.name)
        try {
            val method = brick.javaClass.methods.find {
                it.name.equals(setterName, ignoreCase = true) &&
                        it.parameterCount == 1 &&
                        Formula::class.java.isAssignableFrom(it.parameterTypes[0])
            }
            if (method != null) {
                method.invoke(brick, formula)
                return true
            }
        } catch (ignored: Exception) {}

        return false
    }

    private fun setPrimitiveFieldOnBrick(brick: Brick, fieldName: String, value: Any, sprite: Sprite) {
        var clazz: Class<*>? = brick.javaClass
        while (clazz != null && clazz.name.startsWith("org.catrobat.catroid")) {
            for (method in clazz.declaredMethods) {
                val setterName = "set" + fieldName.replaceFirstChar { it.uppercase() }
                if (method.name.equals(setterName, ignoreCase = true) && method.parameterCount == 1) {
                    method.isAccessible = true
                    val converted = convertValueToType(value, method.parameterTypes[0], sprite)
                    if (converted != null) {
                        method.invoke(brick, converted)
                        return
                    }
                }
            }

            for (field in clazz.declaredFields) {
                if (field.name.equals(fieldName, ignoreCase = true)) {
                    field.isAccessible = true
                    try {
                        val converted = convertValueToType(value, field.type, sprite)
                        if (converted != null) {
                            field.set(brick, converted)
                        }
                    } catch (ignored: Exception) {}
                    return
                }
            }
            clazz = clazz.superclass
        }
    }

    private fun convertValueToType(value: Any, type: Class<*>, sprite: Sprite): Any? {
        return try {
            when {
                type == Int::class.javaPrimitiveType || type == Int::class.java -> {
                    value.toString().toDouble().toInt()
                }
                type == Double::class.javaPrimitiveType || type == Double::class.java -> {
                    value.toString().toDouble()
                }
                type == Float::class.javaPrimitiveType || type == Float::class.java -> {
                    value.toString().toFloat()
                }
                type == Boolean::class.javaPrimitiveType || type == Boolean::class.java -> {
                    value.toString().toBoolean()
                }
                type == String::class.java -> {
                    value.toString().trim('"')
                }
                type.name.contains("LookData") -> {
                    val name = value.toString().trim('"')
                    sprite.lookList.find { it.name == name }
                }
                type.name.contains("SoundInfo") -> {
                    val name = value.toString().trim('"')
                    sprite.soundList.find { it.name == name }
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun addNestedBricksToComposite(composite: CompositeBrick, bricks: List<Brick>, secondary: Boolean) {
        val methodName = if (secondary) "addSecondaryBrick" else "addBrick"
        try {
            val method = composite.javaClass.getMethod(methodName, Brick::class.java)
            for (b in bricks) {
                method.invoke(composite, b)
            }
            return
        } catch (ignored: Exception) {}

        val targetList = if (secondary) {
            composite.secondaryNestedBricks
        } else {
            composite.nestedBricks
        }
        val mutableList = targetList as? MutableList<Brick>
        mutableList?.addAll(bricks)
    }

    private fun extractConstantFromFormula(parsed: ParsedFormulaElement): Any? {
        if (parsed.type == FormulaElementType.NUMBER) return parsed.value
        if (parsed.type == FormulaElementType.STRING) return parsed.value
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

    private fun getShortParamName(fieldName: String): String {
        val categoryMap = mapOf(
            "VALUE" to "val", "VALUE_1" to "val1", "VALUE_2" to "val2", "VALUE_3" to "val3",
            "TIMES_TO_REPEAT" to "times", "INTERVAL" to "interval", "X_POSITION" to "x", "Y_POSITION" to "y",
            "SIZE" to "size", "COLOR" to "color", "STEPS" to "steps", "DEGREES" to "degrees",
            "IF_CONDITION" to "cond", "REPEAT_UNTIL_CONDITION" to "cond", "VARIABLE_CHANGE" to "val",
            "VARIABLE" to "val", "OPEN_URL" to "url", "OPEN_URL_STRING" to "url", "DURATION" to "duration", "VOLUME" to "volume"
        )
        return categoryMap[fieldName.uppercase()] ?: fieldName.lowercase()
    }

    private fun toCamelCase(s: String): String {
        return s.split("_").joinToString("") { part ->
            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    private fun toCamelCaseField(s: String): String {
        val parts = s.split("_")
        if (parts.isEmpty()) return ""
        val first = parts[0].lowercase()
        val rest = parts.drop(1).joinToString("") { part ->
            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        return first + rest
    }
}
