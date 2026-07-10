package org.catrobat.catroid.formulaeditor

import android.util.Log
import com.danvexteam.lunoscript_annotations.LunoClass

@LunoClass
data class CustomFormula(
    val uniqueName: String,
    val displayName: String,
    val paramCount: Int,
    val defaultParamValues: List<String>,
    val defaultParamTypes: List<InternTokenType>,

    val lunoFunctionName: String,
    val ownerLibraryId: String
) {
    init {
        require(defaultParamValues.size == paramCount) {
            "Размер defaultParamValues (${defaultParamValues.size}) должен соответствовать paramCount ($paramCount) для '$uniqueName'"
        }
        require(defaultParamTypes.size == paramCount) {
            "Размер defaultParamTypes (${defaultParamTypes.size}) должен соответствовать paramCount ($paramCount) для '$uniqueName'"
        }
    }
}

@LunoClass
object CustomFormulaManager {
    val formulas: MutableList<CustomFormula> = mutableListOf()

    fun initialize() {

    }

    fun addFormula(formula: CustomFormula) {
        if (formulas.any { it.uniqueName == formula.uniqueName }) {
            Log.w("CustomFormulaManager", "Попытка добавить дублирующуюся формулу: ${formula.uniqueName}")
            return
        }
        if (Functions.isFunction(formula.uniqueName)) {
            Log.e("CustomFormulaManager", "Имя кастомной формулы '${formula.uniqueName}' конфликтует с существующей стандартной функцией!")
            return
        }
        formulas.add(formula)
        Log.i("CustomFormulaManager", "Добавлена кастомная формула: ${formula.uniqueName}")
    }

    fun getFormulaByUniqueName(name: String): CustomFormula? {
        return formulas.find { it.uniqueName == name }
    }

    fun removeFormulasByOwner(libraryId: String) {
        formulas.removeAll { it.ownerLibraryId == libraryId }
        Log.i("CustomFormulaManager", "Удалены формулы, принадлежащие $libraryId")
    }
}
