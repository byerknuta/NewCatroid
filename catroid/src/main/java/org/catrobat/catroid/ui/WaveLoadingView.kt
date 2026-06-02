package org.catrobat.catroid.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import org.catrobat.catroid.R
import java.util.Random

class WaveLoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var size: Float,
        var alpha: Int,
        val maxLife: Int
    ) {
        var life = maxLife

        fun update(): Boolean {
            x += vx
            y += vy
            vx *= 0.91f
            vy *= 0.91f
            life--
            alpha = ((life.toFloat() / maxLife.toFloat()) * 230).toInt().coerceIn(0, 255)
            return life > 0
        }
    }

    private val density = resources.displayMetrics.density
    private val accentColor = ContextCompat.getColor(context, R.color.accent) // #A8DFF4
    private val random = Random()

    private val paintMain = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = accentColor
        strokeWidth = 2.5f * density
        strokeCap = Paint.Cap.ROUND
    }

    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = accentColor
    }

    private val path1 = Path()
    private val particles = ArrayList<Particle>()

    private var phase = 0f
    private var maxAmplitude = 0f
    private var currentAmplitude = 0f

    private var phaseAnimator: ValueAnimator? = null
    private var amplitudeAnimator: ValueAnimator? = null

    init {
        maxAmplitude = 14f * density

        phaseAnimator = ValueAnimator.ofFloat(0f, (2 * Math.PI).toFloat()).apply {
            duration = 1800
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                phase = animation.animatedValue as Float
                invalidate()
            }
        }

        amplitudeAnimator = ValueAnimator.ofFloat(0f, maxAmplitude).apply {
            duration = 1800
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                currentAmplitude = animation.animatedValue as Float
            }
        }
    }

    fun explode(x: Float, y: Float, count: Int) {
        for (i in 0 until count) {
            val angle = random.nextFloat() * 2 * Math.PI
            val speed = (2f + random.nextFloat() * 5.5f) * density
            val vx = Math.cos(angle).toFloat() * speed
            val vy = Math.sin(angle).toFloat() * speed

            val pSize = (1.2f + random.nextFloat() * 2.2f) * density
            val pLife = 20 + random.nextInt(15)

            particles.add(Particle(x, y, vx, vy, pSize, 255, pLife))
        }
        invalidate()
    }

    fun flatline(duration: Long = 350, onComplete: () -> Unit) {
        amplitudeAnimator?.cancel()
        ValueAnimator.ofFloat(currentAmplitude, 0f).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                currentAmplitude = animation.animatedValue as Float
                invalidate()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onComplete()
                }
            })
            start()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        phaseAnimator?.start()
        amplitudeAnimator?.start()
    }

    override fun onDetachedFromWindow() {
        phaseAnimator?.cancel()
        amplitudeAnimator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        val centerY = h * 0.7f

        if (w <= 0 || h <= 0) return

        path1.reset()

        val waveWidth = 240f * density
        val startX = (w - waveWidth) / 2f
        val endX = (w + waveWidth) / 2f

        path1.moveTo(startX, centerY)

        val step = 2f
        var x = startX
        while (x <= endX) {
            val t = (x - startX) / waveWidth
            val envelope = Math.sin(t * Math.PI).toFloat()
            val currentAmp = currentAmplitude * envelope

            val rad1 = t * 2 * Math.PI * 1.5 + phase
            val y1 = centerY + Math.sin(rad1).toFloat() * currentAmp
            path1.lineTo(x, y1)

            x += step
        }

        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            if (p.update()) {
                particlePaint.alpha = p.alpha
                canvas.drawCircle(p.x, p.y, p.size, particlePaint)
            } else {
                iterator.remove()
            }
        }

        canvas.drawPath(path1, paintMain)
    }
}
