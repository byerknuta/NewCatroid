package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class ConfigureParticlesAction : TemporalAction() {
    var scope: Scope? = null
    var particleId: Formula? = null
    var value: Formula? = null
    var configTypeSelection: Int = 0

    override fun update(percent: Float) {
        val idStr = particleId?.interpretString(scope) ?: ""
        val valStr = value?.interpretString(scope) ?: ""

        if (idStr.isEmpty()) return

        val engine = StageActivity.getActiveStageListener()?.threeDManager ?: return

        try {
            val effect = engine.activeParticleEffects[idStr] ?: return

            for (controller in effect.controllers) {
                when (configTypeSelection) {
                    0 -> {
                        val rate = valStr.toFloatOrNull() ?: 10f
                        val emitter = controller.emitter
                        if (emitter is RegularEmitter) {
                            emitter.emission.setHigh(rate)
                            if (rate <= 0.01f) emitter.emission.setLow(0f)
                        }
                    }
                    1 -> {
                        val size = valStr.toFloatOrNull() ?: 1f
                        val scaleInf = controller.influencers.find { it is ScaleInfluencer }
                        if (scaleInf is ScaleInfluencer) {
                            scaleInf.value.setHigh(size)
                        }
                    }
                    2 -> {
                        val angle = valStr.toFloatOrNull() ?: 45f
                        val dynamics = controller.influencers.find { it is com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsInfluencer }
                        if (dynamics is com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsInfluencer) {
                            for (modifier in dynamics.velocities) {
                                if (modifier is DynamicsModifier.PolarAcceleration) {
                                    modifier.thetaValue.setHigh(0f, angle)
                                }
                            }
                        }
                    }
                    3 -> {
                        val maxCount = valStr.toIntOrNull() ?: 100
                        controller.emitter.maxParticleCount = maxCount
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
