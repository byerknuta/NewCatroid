package org.catrobat.catroid.content.actions

import android.util.Log
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.EasingFunctions
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.formulaeditor.InterpretationException
import org.catrobat.catroid.stage.StageActivity

class Ease3DPropertyAction : TemporalAction() {
    private var scope: Scope? = null
    private var objectId: Formula? = null

    private var propertyIndex = 0
    private var typeIndex = 0

    private var durationFormula: Formula? = null
    private var startFormula: Formula? = null
    private var endFormula: Formula? = null

    private var startVal = 0f
    private var endVal = 0f

    private val easingTypes = EasingFunctions.EasingType.entries.toTypedArray()

    override fun begin() {
        try {
            val durationVal = durationFormula?.interpretFloat(scope) ?: 1f
            duration = durationVal

            startVal = startFormula?.interpretFloat(scope) ?: 0f
            endVal = endFormula?.interpretFloat(scope) ?: 0f
        } catch (e: InterpretationException) {
            Log.d(javaClass.simpleName, "Formula interpretation failed in Ease3DPropertyAction.", e)
            duration = 0f
        }
        super.begin()
    }

    override fun update(percent: Float) {
        val stageListener = StageActivity.getActiveStageListener() ?: return
        val sceneMgr = stageListener.sceneManager
        val threeD = stageListener.threeDManager ?: return

        val idStr = objectId?.interpretString(scope) ?: return
        if (idStr.isEmpty()) return

        val currentTime = time.coerceAtMost(duration)
        val totalDuration = duration

        val safeIndex = if (typeIndex >= 0 && typeIndex < easingTypes.size) typeIndex else 0
        val easingType = easingTypes[safeIndex]

        val result = EasingFunctions.calculate(easingType, currentTime, totalDuration, startVal, endVal)

        val gameObject = sceneMgr?.findGameObject(idStr)

        if (gameObject != null) {
            when (propertyIndex) {
                0 -> gameObject.transform.position.x = result
                1 -> gameObject.transform.position.y = result
                2 -> gameObject.transform.position.z = result
                3, 4, 5 -> {
                    val q = gameObject.transform.rotation
                    val yaw = q.yaw
                    val pitch = q.pitch
                    val roll = q.roll
                    when (propertyIndex) {
                        3 -> gameObject.transform.rotation.setEulerAngles(result, pitch, roll)
                        4 -> gameObject.transform.rotation.setEulerAngles(yaw, result, roll)
                        5 -> gameObject.transform.rotation.setEulerAngles(yaw, pitch, result)
                    }
                }
                6 -> gameObject.transform.scale.x = result
                7 -> gameObject.transform.scale.y = result
                8 -> gameObject.transform.scale.z = result
            }
        } else {
            val pos = threeD.getPosition(idStr) ?: com.badlogic.gdx.math.Vector3()
            val rot = threeD.getRotation(idStr) ?: com.badlogic.gdx.math.Vector3()
            val scale = threeD.getScale(idStr) ?: com.badlogic.gdx.math.Vector3(1f, 1f, 1f)

            when (propertyIndex) {
                0 -> threeD.setPosition(idStr, result, pos.y, pos.z)
                1 -> threeD.setPosition(idStr, pos.x, result, pos.z)
                2 -> threeD.setPosition(idStr, pos.x, pos.y, result)
                3 -> threeD.setRotation(idStr, result, rot.x, rot.z)
                4 -> threeD.setRotation(idStr, rot.y, result, rot.z)
                5 -> threeD.setRotation(idStr, rot.y, rot.x, result)
                6 -> threeD.setScale(idStr, result, scale.y, scale.z)
                7 -> threeD.setScale(idStr, scale.x, result, scale.z)
                8 -> threeD.setScale(idStr, scale.x, scale.y, result)
            }
        }
    }

    override fun reset() {
        super.reset()
        scope = null
        objectId = null
        durationFormula = null
        startFormula = null
        endFormula = null
    }

    fun setScope(scope: Scope?) { this.scope = scope }
    fun setObjectId(formula: Formula?) { this.objectId = formula }
    fun setPropertyIndex(index: Int) { this.propertyIndex = index }
    fun setTypeIndex(index: Int) { this.typeIndex = index }
    fun setDurationFormula(formula: Formula?) { this.durationFormula = formula }
    fun setStartFormula(formula: Formula?) { this.startFormula = formula }
    fun setEndFormula(formula: Formula?) { this.endFormula = formula }
}
