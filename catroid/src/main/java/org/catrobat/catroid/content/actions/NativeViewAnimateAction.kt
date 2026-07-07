package org.catrobat.catroid.content.actions

import android.view.animation.Interpolator
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.EasingFunctions
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class NativeViewAnimateAction : TemporalAction() {
    var scope: Scope? = null
    var viewId: Formula? = null
    var value: Formula? = null
    var durationMs: Formula? = null
    var propertySelection: Int = 0
    var easingSelection: Int = 0

    private class CustomEasingInterpolator(private val easingType: EasingFunctions.EasingType) : Interpolator {
        override fun getInterpolation(input: Float): Float {
            return EasingFunctions.calculate(easingType, input, 1.0f, 0.0f, 1.0f)
        }
    }

    override fun update(percent: Float) {
        val stage = StageActivity.activeStageActivity?.get() ?: return
        val idStr = viewId?.interpretString(scope) ?: return
        val targetVal = value?.interpretFloat(scope) ?: 0.0f
        val timeMs = durationMs?.interpretInteger(scope)?.toLong() ?: 300L

        val view = stage.getViewFromStage(idStr) ?: return

        stage.runOnUiThread {
            try {
                val easingType = EasingFunctions.EasingType.values()[easingSelection]

                if (propertySelection in 6..7) {
                    val startVal = if (propertySelection == 6) stage.getViewX(idStr) else stage.getViewY(idStr)

                    val valueAnimator = android.animation.ValueAnimator.ofFloat(startVal, targetVal).apply {
                        duration = timeMs
                        interpolator = CustomEasingInterpolator(easingType)
                        addUpdateListener { animation ->
                            val animatedValue = animation.animatedValue as Float
                            if (propertySelection == 6) {
                                stage.setViewPosition(idStr, animatedValue.toInt(), stage.getViewY(idStr).toInt())
                            } else {
                                stage.setViewPosition(idStr, stage.getViewX(idStr).toInt(), animatedValue.toInt())
                            }
                        }
                    }
                    valueAnimator.start()
                } else {
                    val animator = view.animate()
                        .setDuration(timeMs)
                        .setInterpolator(CustomEasingInterpolator(easingType))

                    when (propertySelection) {
                        0 -> animator.alpha(targetVal)
                        1 -> animator.rotation(targetVal)
                        2 -> animator.scaleX(targetVal)
                        3 -> animator.scaleY(targetVal)
                        4 -> animator.translationX(targetVal)
                        5 -> animator.translationY(targetVal)
                    }
                    animator.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun restart() {
        super.restart()
    }
}
