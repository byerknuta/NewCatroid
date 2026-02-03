package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.EasingFunctions
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.formulaeditor.UserVariable

class SetVariableEasingAction : TemporalAction() {
    private var scope: Scope? = null
    private var userVariable: UserVariable? = null


    private var typeIndex: Int = 0

    private var timeFormula: Formula? = null
    private var durationFormula: Formula? = null
    private var startFormula: Formula? = null
    private var endFormula: Formula? = null


    private val easingTypes = EasingFunctions.EasingType.values()

    override fun update(percent: Float) {
        if (userVariable == null) return

        val currentTime = timeFormula?.interpretFloat(scope) ?: 0f
        val duration = durationFormula?.interpretFloat(scope) ?: 1f
        val startVal = startFormula?.interpretFloat(scope) ?: 0f
        val endVal = endFormula?.interpretFloat(scope) ?: 0f


        val safeIndex = if (typeIndex >= 0 && typeIndex < easingTypes.size) typeIndex else 0
        val easingType = easingTypes[safeIndex]

        val result = EasingFunctions.calculate(easingType, currentTime, duration, startVal, endVal)
        userVariable!!.value = result.toDouble()
    }


    fun setScope(scope: Scope?) { this.scope = scope }
    fun setUserVariable(userVariable: UserVariable?) { this.userVariable = userVariable }
    fun setTypeIndex(index: Int) { this.typeIndex = index }
    fun setTimeFormula(formula: Formula?) { this.timeFormula = formula }
    fun setDurationFormula(formula: Formula?) { this.durationFormula = formula }
    fun setStartFormula(formula: Formula?) { this.startFormula = formula }
    fun setEndFormula(formula: Formula?) { this.endFormula = formula }
}