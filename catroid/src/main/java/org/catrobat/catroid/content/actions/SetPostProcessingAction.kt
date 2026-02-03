package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.raptor.PostProcessingComponent
import org.catrobat.catroid.raptor.PostProcessingData
import org.catrobat.catroid.stage.StageActivity
import kotlin.math.max
import kotlin.math.min

class SetPostProcessingAction : TemporalAction() {

    var scope: Scope? = null
    var effectIndex: Int = 0
    var paramIndex: Int = 0
    var valueFormula: Formula? = null

    override fun update(percent: Float) {
        val stageListener = StageActivity.getActiveStageListener() ?: return
        val threeDManager = stageListener.threeDManager ?: return

        // 1. Получаем конфиг (или создаем дефолтный, если проект без сцен)
        var config = threeDManager.currentConfig
        if (config == null) {
            config = PostProcessingComponent()
            threeDManager.updatePostProcessing(config)
        }

        // 2. Считаем значение из формулы
        val floatVal = valueFormula?.interpretFloat(scope) ?: 0f
        val boolVal = floatVal > 0.5f

        // 3. Применяем настройки
        if (effectIndex == 0) {
            // --- GLOBAL ---
            when (paramIndex) {
                0 -> config.isActive = boolVal
                5 -> config.qualityScale = floatVal.coerceIn(0.1f, 1.0f)
            }
        } else {
            // --- EFFECTS ---
            val targetEffect = findOrCreateEffect(config, effectIndex)
            if (targetEffect != null) {
                applyParam(targetEffect, paramIndex, floatVal, boolVal)
            }
        }

        // 4. Обновляем движок
        threeDManager.updatePostProcessing(config)
    }

    private fun findOrCreateEffect(config: PostProcessingComponent, typeIndex: Int): PostProcessingData? {
        // Маппинг индексов из pp_effect_types
        val targetClass: Class<out PostProcessingData> = when (typeIndex) {
            1 -> PostProcessingData.Bloom::class.java
            2 -> PostProcessingData.Vignette::class.java
            3 -> PostProcessingData.Levels::class.java
            4 -> PostProcessingData.Grain::class.java
            5 -> PostProcessingData.Fxaa::class.java
            6 -> PostProcessingData.Chromatic::class.java
            7 -> PostProcessingData.RadialBlur::class.java
            8 -> PostProcessingData.OldTv::class.java
            9 -> PostProcessingData.Crt::class.java
            10 -> PostProcessingData.Fisheye::class.java
            11 -> PostProcessingData.Water::class.java
            12 -> PostProcessingData.MotionBlur::class.java
            13 -> PostProcessingData.LensFlare::class.java
            14 -> PostProcessingData.Gaussian::class.java
            15 -> PostProcessingData.Zoom::class.java
            16 -> PostProcessingData.ACES::class.java
            17 -> PostProcessingData.EyeAdaptation::class.java
            else -> return null
        }

        // Ищем существующий или создаем новый
        val existing = config.effects.find { targetClass.isInstance(it) }
        if (existing != null) return existing

        return try {
            val newEffect = targetClass.newInstance()
            newEffect.isEnabled = true
            config.effects.add(newEffect)
            newEffect
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun applyParam(data: PostProcessingData, paramIdx: Int, valFloat: Float, valBool: Boolean) {
        // 0 = Enabled (для всех)
        if (paramIdx == 0) {
            data.isEnabled = valBool
            return
        }

        when (data) {
            is PostProcessingData.Bloom -> {
                when (paramIdx) {
                    1 -> data.intensity = valFloat        // Intensity
                    2 -> data.threshold = valFloat        // Threshold
                    3 -> data.blurAmount = valFloat       // Blur Amount
                    4 -> data.blurPasses = max(1, valFloat.toInt()) // Blur Passes
                }
            }
            is PostProcessingData.Levels -> {
                when (paramIdx) {
                    6 -> data.contrast = valFloat         // Contrast
                    7 -> data.saturation = valFloat       // Saturation
                    8 -> data.gamma = valFloat            // Gamma
                }
            }
            is PostProcessingData.Vignette -> {
                when (paramIdx) {
                    1 -> data.intensity = valFloat        // Intensity
                    7 -> data.saturation = valFloat       // Saturation
                }
            }
            is PostProcessingData.Grain -> {
                if (paramIdx == 1) data.amount = valFloat // Amount
            }
            is PostProcessingData.Chromatic -> {
                when (paramIdx) {
                    1 -> data.strength = valFloat         // Strength
                    10 -> data.maxDistortion = valFloat   // Max Distort
                }
            }
            is PostProcessingData.RadialBlur -> {
                when (paramIdx) {
                    1 -> data.strength = valFloat         // Strength
                    4 -> data.blurPasses = max(1, valFloat.toInt()) // Blur Passes
                    3 -> data.size = valFloat
                }
            }
            is PostProcessingData.OldTv -> {
                if (paramIdx == 13 || paramIdx == 1) data.strength = valFloat // Noise (using Noise Idx 13 or Strength 1)
            }
            is PostProcessingData.Crt -> {
                // Если добавил поля в PostProcessingData.Crt:
                // if (paramIdx == 10) data.distortion = valFloat
                // if (paramIdx == 1) data.zoom = valFloat
            }
            is PostProcessingData.Fisheye -> {
                // Если есть поля
            }
            is PostProcessingData.Water -> {
                when (paramIdx) {
                    1 -> data.amount = valFloat           // Amount
                    9 -> data.speed = valFloat            // Speed
                }
            }
            is PostProcessingData.MotionBlur -> {
                // Blur Opacity (мапим на Intensity/Strength - индекс 1)
                if (paramIdx == 1) data.blurOpacity = valFloat.coerceIn(0f, 0.99f)
            }
            is PostProcessingData.LensFlare -> {
                when (paramIdx) {
                    1 -> data.intensity = valFloat        // Intensity
                    2 -> data.threshold = valFloat        // Threshold
                }
            }
            is PostProcessingData.Gaussian -> {
                when (paramIdx) {
                    1 -> data.amount = valFloat           // Amount
                    4 -> data.passes = max(1, valFloat.toInt()) // Blur Passes
                    3 -> data.size = valFloat
                }
            }
            is PostProcessingData.Zoom -> {
                when (paramIdx) {
                    1 -> data.zoom = valFloat             // Strength (Zoom)
                    11 -> data.originX = valFloat         // Origin X
                    12 -> data.originY = valFloat         // Origin Y
                }
            }
            is PostProcessingData.ACES -> {
            }
            is PostProcessingData.EyeAdaptation -> {
                when (paramIdx) {
                    1 -> data.targetLuminance = valFloat
                    9 -> data.speed = valFloat
                    14 -> data.maxExposure = valFloat
                    15 -> data.minExposure = valFloat
                }
            }
        }
    }
}