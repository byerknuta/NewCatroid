package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import com.badlogic.gdx.math.Vector3
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class ConfigureLightAction : TemporalAction() {
    var scope: Scope? = null
    var lightId: Formula? = null
    var value: Formula? = null
    var configTypeSelection: Int = 0

    override fun update(percent: Float) {
        val idStr = lightId?.interpretString(scope) ?: ""
        val valStr = value?.interpretString(scope) ?: ""

        if (idStr.isEmpty()) return

        val engine = StageActivity.getActiveStageListener()?.threeDManager ?: return

        try {
            val pointLight = engine.pointLights[idStr]
            val spotLight = engine.spotLights[idStr]

            if (pointLight == null && spotLight == null) return

            when (configTypeSelection) {
                0 -> {
                    val intensity = valStr.toFloatOrNull() ?: 1f
                    pointLight?.intensity = intensity
                    spotLight?.intensity = intensity
                }
                1 -> {
                    val range = valStr.toFloatOrNull() ?: 10f
                    pointLight?.range = if (range > 0) range else null
                    spotLight?.range = if (range > 0) range else null
                }
                2 -> {
                    val rgb = valStr.split(",").map { it.trim().toFloatOrNull() ?: 1f }
                    if (rgb.size >= 3) {
                        pointLight?.color?.set(rgb[0], rgb[1], rgb[2], 1f)
                        spotLight?.color?.set(rgb[0], rgb[1], rgb[2], 1f)
                    }
                }
                3 -> {
                    val pos = valStr.split(",").map { it.trim().toFloatOrNull() ?: 0f }
                    if (pos.size >= 3) {
                        pointLight?.position?.set(pos[0], pos[1], pos[2])
                        spotLight?.position?.set(pos[0], pos[1], pos[2])
                    }
                }
                4 -> {
                    val dir = valStr.split(",").map { it.trim().toFloatOrNull() ?: 0f }
                    if (dir.size >= 3 && spotLight != null) {
                        spotLight.direction.set(dir[0], dir[1], dir[2]).nor()
                    }
                }
                5 -> {
                    val angle = valStr.toFloatOrNull() ?: 45f
                    if (spotLight != null) {
                        val currentSoftness = spotLight.cutoffAngle
                        spotLight.setConeDeg(angle, angle * 0.8f)
                    }
                }
                6 -> {
                    val softness = valStr.toFloatOrNull() ?: 0.5f
                    if (spotLight != null) {
                        val cutoff = spotLight.cutoffAngle
                        val inner = cutoff * (1.0f - softness.coerceIn(0f, 1f))
                        spotLight.setConeDeg(cutoff, inner)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
