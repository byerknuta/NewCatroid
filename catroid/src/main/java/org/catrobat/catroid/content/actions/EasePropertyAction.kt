package org.catrobat.catroid.content.actions

import android.util.Log
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.EasingFunctions
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.formulaeditor.InterpretationException

class EasePropertyAction : TemporalAction() {
    private var scope: Scope? = null

    private var propertyIndex = 0
    private var typeIndex = 0

    private var durationFormula: Formula? = null
    private var startFormula: Formula? = null
    private var endFormula: Formula? = null

    private var startVal = 0f
    private var endVal = 0f

    private val easingTypes = EasingFunctions.EasingType.values()

    override fun begin() {
        try {
            val durationVal = durationFormula?.interpretFloat(scope) ?: 1f
            duration = durationVal

            startVal = startFormula?.interpretFloat(scope) ?: 0f
            endVal = endFormula?.interpretFloat(scope) ?: 0f
        } catch (e: InterpretationException) {
            Log.d(javaClass.simpleName, "Formula interpretation failed in EasePropertyAction.", e)
            duration = 0f
        }
        super.begin()
    }

    override fun update(percent: Float) {
        val sprite = scope?.sprite ?: return
        val look = sprite.look ?: return

        val currentTime = Math.min(time, duration)
        val totalDuration = duration

        val safeIndex = if (typeIndex >= 0 && typeIndex < easingTypes.size) typeIndex else 0
        val easingType = easingTypes[safeIndex]

        val result = EasingFunctions.calculate(easingType, currentTime, totalDuration, startVal, endVal)

        when (propertyIndex) {
            0 -> look.setPositionInUserInterfaceDimensionUnit(result, look.getYInUserInterfaceDimensionUnit())
            1 -> look.setPositionInUserInterfaceDimensionUnit(look.getXInUserInterfaceDimensionUnit(), result)
            2 -> look.setSizeInUserInterfaceDimensionUnit(result)
            3 -> look.setTransparencyInUserInterfaceDimensionUnit(result)
            4 -> look.setColorInUserInterfaceDimensionUnit(result)
            5 -> look.setWidthV(result / 100f)
            6 -> look.setHeightV(result / 100f)
            7 -> look.brightnessInUserInterfaceDimensionUnit = result
        }
    }

    fun setScope(scope: Scope?) { this.scope = scope }
    fun setPropertyIndex(index: Int) { this.propertyIndex = index }
    fun setTypeIndex(index: Int) { this.typeIndex = index }
    fun setDurationFormula(formula: Formula?) { this.durationFormula = formula }
    fun setStartFormula(formula: Formula?) { this.startFormula = formula }
    fun setEndFormula(formula: Formula?) { this.endFormula = formula }
}
