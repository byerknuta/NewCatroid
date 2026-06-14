package org.catrobat.catroid.ui.recyclerview.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import org.catrobat.catroid.R

class IndentedBrickLayout(context: Context, rawDepth: Int) : LinearLayout(context) {
    companion object {
        const val MAX_DEPTH = 5
    }

    private var depth = 0
    private val indentWidth: Int
    private val linePaint: Paint

    init {
        orientation = HORIZONTAL
        setWillNotDraw(false)

        val density = context.resources.displayMetrics.density
        this.indentWidth = (14 * density).toInt()

        val lineColor = ContextCompat.getColor(context, R.color.separator)

        linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lineColor
            strokeWidth = 1.25f * density
            style = Paint.Style.STROKE
        }

        setDepth(rawDepth)
    }

    fun setDepth(rawDepth: Int) {
        this.depth = kotlin.math.min(rawDepth, MAX_DEPTH)
        setPadding(this.depth * indentWidth, 0, 0, 0)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (depth > 0) {
            val height = height
            for (i in 0 until depth) {
                val x = (i * indentWidth) + (indentWidth / 2f)
                canvas.drawLine(x, 0f, x, height.toFloat(), linePaint)
            }
        }
    }
}
