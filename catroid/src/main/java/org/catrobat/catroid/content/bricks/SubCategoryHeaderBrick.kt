package org.catrobat.catroid.content.bricks

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.catrobat.catroid.R
import org.catrobat.catroid.ui.BrickLayout

class SubCategoryHeaderView(context: Context) : FrameLayout(context) {
    init {
        clipChildren = false
        clipToPadding = false
    }
}

class OutlinedNinePatchDrawable(
    private val original: Drawable,
    private val outlineColor: Int,
    private val outlineSizePx: Int,
    private val safetyMarginHorizontal: Int,
    private val safetyMarginVertical: Int,
    density: Float
) : Drawable() {

    private val whiteFilter = PorterDuffColorFilter(outlineColor, PorterDuff.Mode.SRC_IN)
    private val offsets = ArrayList<Pair<Int, Int>>()

    init {
        val steps = 12
        for (i in 0 until steps) {
            val angle = i * 2 * Math.PI / steps
            val dx = (outlineSizePx * Math.cos(angle)).toInt()
            val dy = (outlineSizePx * Math.sin(angle)).toInt()
            offsets.add(dx to dy)
        }
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds

        val drawBounds = Rect(
            bounds.left + safetyMarginHorizontal,
            bounds.top + safetyMarginVertical,
            bounds.right - safetyMarginHorizontal,
            bounds.bottom - safetyMarginVertical
        )

        original.colorFilter = whiteFilter

        for ((dx, dy) in offsets) {
            original.setBounds(drawBounds.left + dx, drawBounds.top + dy, drawBounds.right + dx, drawBounds.bottom + dy)
            original.draw(canvas)
        }

        original.setBounds(drawBounds.left, drawBounds.top, drawBounds.right, drawBounds.bottom)
        original.draw(canvas)

        original.bounds = drawBounds
        original.colorFilter = null
        original.draw(canvas)

        original.bounds = bounds
    }

    override fun setAlpha(alpha: Int) {
        original.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        original.bounds = bounds
    }
}

class SubCategoryHeaderBrick(val title: String, val templateBrick: Brick) : NoteBrick() {
    var isExpanded: Boolean = true
    var isStacked: Boolean = false

    override fun getPrototypeView(context: Context): View {
        val density = context.resources.displayMetrics.density

        val container = SubCategoryHeaderView(context).apply {
            layoutParams = android.widget.AbsListView.LayoutParams(
                android.widget.AbsListView.LayoutParams.MATCH_PARENT,
                android.widget.AbsListView.LayoutParams.WRAP_CONTENT
            )
            val pv = 0
            setPadding(0, pv, 0, pv)
        }

        val nativeView = templateBrick.getPrototypeView(context).apply {
            if (this is ViewGroup) {
                clipChildren = false
                clipToPadding = false
            }
        }

        val brickLayout = findBrickLayout(nativeView)
        if (brickLayout != null) {
            if (brickLayout is ViewGroup) {
                brickLayout.clipChildren = false
                brickLayout.clipToPadding = false
            }

            val outlineSize = (2f * density).toInt().coerceAtLeast(2)
            val safetyMarginHorizontal = outlineSize + (4 * density).toInt()
            val safetyMarginVertical = outlineSize + (2 * density).toInt()

            val originalBg = brickLayout.background
            if (originalBg != null) {
                brickLayout.background = OutlinedNinePatchDrawable(
                    originalBg, Color.WHITE, outlineSize,
                    safetyMarginHorizontal, safetyMarginVertical, density
                )
            }

            val origPadLeft = brickLayout.paddingLeft
            val origPadTop = brickLayout.paddingTop
            val origPadRight = brickLayout.paddingRight
            val origPadBottom = brickLayout.paddingBottom

            brickLayout.setPadding(
                origPadLeft + safetyMarginHorizontal,
                origPadTop + safetyMarginVertical,
                origPadRight + safetyMarginHorizontal,
                origPadBottom + safetyMarginVertical
            )

            brickLayout.removeAllViews()

            val titleView = TextView(context).apply {
                id = R.id.brick_header_title
                text = title
                textSize = 17f
                setTextColor(ContextCompat.getColor(context, R.color.solid_white))
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setPadding(0, 0, (32 * density).toInt(), 0)
            }
            brickLayout.addView(titleView)

            val topMarginDp = 6
            val bottomMarginDp = 2

            var params = brickLayout.layoutParams as? ViewGroup.MarginLayoutParams
            if (params == null) {
                params = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            params.setMargins(
                params.leftMargin,
                (topMarginDp * density).toInt(),
                params.rightMargin,
                (bottomMarginDp * density).toInt()
            )
            brickLayout.layoutParams = params
        }

        val arrowView = TextView(context).apply {
            id = R.id.brick_header_arrow
            text = if (isExpanded) "▼" else "▶"
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.solid_white))
            gravity = Gravity.CENTER

            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.END
                rightMargin = (24 * density).toInt()
            }
        }

        nativeView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        container.addView(nativeView)
        container.addView(arrowView)

        return container
    }

    private fun findBrickLayout(view: View): BrickLayout? {
        if (view is BrickLayout) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val result = findBrickLayout(child)
                if (result != null) return result
            }
        }
        return null
    }

    @SuppressLint("MissingSuperCall")
    override fun getView(context: Context): View? {
        return null
    }
}
