package org.catrobat.catroid.ui.workspace

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.catrobat.catroid.R
import org.catrobat.catroid.stage.StageActivity
import androidx.core.graphics.toColorInt

class WindowContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    lateinit var windowTag: String
    private lateinit var titleTextView: TextView
    lateinit var pinButton: ImageView
    private lateinit var closeButton: ImageView
    lateinit var contentFrame: FrameLayout
    private lateinit var resizeHandle: ImageView

    var isPinned = false

    private var initialRawX = 0f
    private var initialRawY = 0f
    private var initialLeft = 0
    private var initialTop = 0
    private var initialWidth = 0
    private var initialHeight = 0

    private val minWidth = dpToPx(240)
    private val minHeight = dpToPx(180)

    var onLayoutChangedListener: (() -> Unit)? = null
    var onCloseClickListener: (() -> Unit)? = null

    lateinit var optionsButton: ImageView

    private lateinit var maximizeButton: ImageView
    var isMaximized = false
    private var savedWidth = 0
    private var savedHeight = 0
    private var savedLeft = 0
    private var savedTop = 0

    init {
        orientation = VERTICAL
        elevation = dpToPx(8).toFloat()

        applyGlassStyle(false)
        setupViews()

        alpha = 0f
        scaleX = 0.85f
        scaleY = 0.85f
    }

    private fun setupViews() {
        val titleBar = LinearLayout(context).apply {
            orientation = HORIZONTAL
            setBackgroundColor("#B0002B4D".toColorInt())
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
            gravity = Gravity.CENTER_VERTICAL
        }

        titleTextView = TextView(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            setTextColor(ContextCompat.getColor(context, R.color.solid_white))
            textSize = 14f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        pinButton = ImageView(context).apply {
            layoutParams = LayoutParams(dpToPx(20), dpToPx(20)).apply {
                rightMargin = dpToPx(12)
            }
            setImageResource(R.drawable.ic_pin)
            setColorFilter(ContextCompat.getColor(context, R.color.accent))
            rotation = -45f
            alpha = 0.6f

            setOnClickListener {
                isPinned = !isPinned
                updatePinIconState()
                animateScale(this, 1.2f)

                onLayoutChangedListener?.invoke()
            }
        }

        optionsButton = ImageView(context).apply {
            layoutParams = LayoutParams(dpToPx(20), dpToPx(20)).apply {
                rightMargin = dpToPx(12)
            }
            setImageResource(R.drawable.ic_more_vert)
            setColorFilter(ContextCompat.getColor(context, R.color.accent))
            visibility = GONE

            setOnClickListener {
                animateScale(this, 1.2f)
            }
        }

        maximizeButton = ImageView(context).apply {
            layoutParams = LayoutParams(dpToPx(20), dpToPx(20)).apply {
                rightMargin = dpToPx(12)
            }
            setImageResource(R.drawable.ic_smartphone_dialog_orientation_landscape)
            setColorFilter(ContextCompat.getColor(context, R.color.accent))
            setOnClickListener {
                toggleMaximize()
                animateScale(this, 1.2f)
            }
        }

        closeButton = ImageView(context).apply {
            layoutParams = LayoutParams(dpToPx(22), dpToPx(22))
            setImageResource(R.drawable.ic_close)
            setColorFilter(ContextCompat.getColor(context, R.color.accent))
            setOnClickListener {
                animate()
                    .alpha(0f)
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(150)
                    .withEndAction { onCloseClickListener?.invoke() }
                    .start()
            }
        }

        titleBar.addView(titleTextView)
        titleBar.addView(optionsButton)
        titleBar.addView(maximizeButton)
        titleBar.addView(pinButton)
        titleBar.addView(closeButton)
        addView(titleBar)

        val contentContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
        }

        contentFrame = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        contentContainer.addView(contentFrame)

        resizeHandle = ImageView(context).apply {
            val size = dpToPx(20)
            layoutParams = FrameLayout.LayoutParams(size, size).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                rightMargin = dpToPx(4)
                bottomMargin = dpToPx(4)
            }
            setImageResource(R.drawable.ic_pocketpaint_tool_resize_adjust)
            setColorFilter(ContextCompat.getColor(context, R.color.accent))
        }
        contentContainer.addView(resizeHandle)
        addView(contentContainer)

        setupDragAndResize()
    }

    fun initWindow(tag: String, title: String) {
        this.windowTag = tag
        this.titleTextView.text = title
        this.contentFrame.id = (tag.hashCode() and 0x00FFFFFF) or 0x00010000

        if (tag == "StageGame") {
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
        } else {
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(250)
                .setInterpolator(OvershootInterpolator(1.05f))
                .start()
        }
    }

    fun updatePinIconState() {
        val targetRotation = if (isPinned) 0f else -45f
        pinButton.rotation = targetRotation
        pinButton.alpha = if (isPinned) 1.0f else 0.6f
        resizeHandle.visibility = if (isPinned) GONE else VISIBLE
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val parentLayout = parent as? WorkspaceLayout
            if (parentLayout != null) {
                parentLayout.requestFocus(windowTag)
                bringToFront()
                parentLayout.bringNavigationToFront()
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDragAndResize() {
        getChildAt(0).setOnTouchListener { _, event ->
            val parentView = parent as? ViewGroup ?: return@setOnTouchListener false
            val workspace = parent as? WorkspaceLayout

            if (isMaximized) return@setOnTouchListener true

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialRawX = event.rawX
                    initialRawY = event.rawY
                    val params = layoutParams as FrameLayout.LayoutParams
                    initialLeft = params.leftMargin
                    initialTop = params.topMargin

                    bringToFront()
                    workspace?.bringNavigationToFront()

                    if (!isPinned && workspace != null) {
                        workspace.startGhostDrag(this)
                    }

                    setFocusedState(true)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isPinned) return@setOnTouchListener true

                    val deltaX = event.rawX - initialRawX
                    val deltaY = event.rawY - initialRawY
                    val params = layoutParams as FrameLayout.LayoutParams

                    val newLeft = (initialLeft + deltaX).toInt()
                    val newTop = (initialTop + deltaY).toInt()

                    if (workspace != null) {
                        params.leftMargin = newLeft
                        params.topMargin = newTop
                        layoutParams = params
                        workspace.updateGhostDrag(this, params.leftMargin, params.topMargin)
                    } else {
                        val pWidth = parentView.width.takeIf { it > 0 } ?: context.resources.displayMetrics.widthPixels
                        val pHeight = parentView.height.takeIf { it > 0 } ?: context.resources.displayMetrics.heightPixels

                        val maxLeft = (pWidth - width).coerceAtLeast(0)
                        val maxTop = (pHeight - height).coerceAtLeast(0)
                        params.leftMargin = newLeft.coerceIn(0, maxLeft)
                        params.topMargin = newTop.coerceIn(0, maxTop)
                        layoutParams = params
                    }
                }
                MotionEvent.ACTION_UP -> {
                    setFocusedState(false)
                    if (isPinned) return@setOnTouchListener true

                    val params = layoutParams as FrameLayout.LayoutParams
                    if (workspace != null) {
                        val finalCoords = workspace.calculateSnap(this, params.leftMargin, params.topMargin, width, height)
                        workspace.endGhostDrag(this, finalCoords.first, finalCoords.second)
                    } else {
                        onLayoutChangedListener?.invoke()
                    }
                }
            }
            true
        }

        resizeHandle.setOnTouchListener { _, event ->
            if (isPinned || isMaximized) return@setOnTouchListener true

            val parentView = parent as? ViewGroup ?: return@setOnTouchListener false
            val workspace = parent as? WorkspaceLayout

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialRawX = event.rawX
                    initialRawY = event.rawY
                    initialWidth = width
                    initialHeight = height
                    setResizingState(true)

                    if (workspace != null) {
                        workspace.startGhostDrag(this)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialRawX
                    val deltaY = event.rawY - initialRawY
                    val params = layoutParams as FrameLayout.LayoutParams

                    val newWidth = (initialWidth + deltaX).toInt().coerceAtLeast(minWidth)
                    val newHeight = (initialHeight + deltaY).toInt().coerceAtLeast(minHeight)

                    val pWidth = parentView.width.takeIf { it > 0 } ?: context.resources.displayMetrics.widthPixels
                    val pHeight = parentView.height.takeIf { it > 0 } ?: context.resources.displayMetrics.heightPixels

                    if (workspace != null) {
                        if (windowTag != "StageGame") {
                            params.width = newWidth
                            params.height = newHeight
                            layoutParams = params
                        }

                        val (snapWidth, snapHeight) = workspace.calculateResizeSnap(this, newWidth, newHeight)
                        workspace.updateGhostResize(this, params.leftMargin, params.topMargin, snapWidth, snapHeight)
                    } else {
                        val maxResizeWidth = (pWidth - params.leftMargin).coerceAtLeast(minWidth)
                        val maxResizeHeight = (pHeight - params.topMargin).coerceAtLeast(minHeight)
                        params.width = newWidth.coerceIn(minWidth, maxResizeWidth)
                        params.height = newHeight.coerceIn(minHeight, maxResizeHeight)
                        layoutParams = params
                    }
                }
                MotionEvent.ACTION_UP -> {
                    setResizingState(false)
                    val params = layoutParams as FrameLayout.LayoutParams
                    if (workspace != null) {
                        val (snapWidth, snapHeight) = workspace.calculateResizeSnap(this, params.width, params.height)
                        workspace.endGhostResize(this, snapWidth, snapHeight)
                    } else {
                        onLayoutChangedListener?.invoke()
                    }
                }
            }
            true
        }
    }

    fun animateToPosition(targetLeft: Int, targetTop: Int, onEnd: (() -> Unit)? = null) {
        val params = layoutParams as FrameLayout.LayoutParams

        if (windowTag == "StageGame") {
            params.leftMargin = targetLeft
            params.topMargin = targetTop
            layoutParams = params
            onEnd?.invoke()
            return
        }

        val startLeft = params.leftMargin
        val startTop = params.topMargin

        android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 200
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float
                params.leftMargin = (startLeft + (targetLeft - startLeft) * fraction).toInt()
                params.topMargin = (startTop + (targetTop - startTop) * fraction).toInt()
                layoutParams = params
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onEnd?.invoke()
                }
            })
        }.start()
    }

    fun animateToSize(targetWidth: Int, targetHeight: Int, onEnd: (() -> Unit)? = null) {
        val params = layoutParams as FrameLayout.LayoutParams

        if (windowTag == "StageGame") {
            params.width = targetWidth
            params.height = targetHeight
            layoutParams = params
            val activity = context as? StageActivity
            activity?.updateStageSize(targetWidth, targetHeight)
            onEnd?.invoke()
            return
        }

        val startWidth = width
        val startHeight = height

        android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 180
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float
                params.width = (startWidth + (targetWidth - startWidth) * fraction).toInt()
                params.height = (startHeight + (targetHeight - startHeight) * fraction).toInt()
                layoutParams = params
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onEnd?.invoke()
                }
            })
        }.start()
    }

    fun toggleMaximize() {
        val parentView = parent as? ViewGroup ?: return
        val params = layoutParams as FrameLayout.LayoutParams
        val titleBar = getChildAt(0)

        if (!isMaximized) {
            savedWidth = params.width
            savedHeight = params.height
            savedLeft = params.leftMargin
            savedTop = params.topMargin

            titleBar.visibility = GONE
            resizeHandle.visibility = GONE
            setPadding(0, 0, 0, 0)

            animateToPosition(0, 0)
            animateToSize(parentView.width, parentView.height) {
                params.leftMargin = 0
                params.topMargin = 0
                params.width = LayoutParams.MATCH_PARENT
                params.height = LayoutParams.MATCH_PARENT
                layoutParams = params
                isMaximized = true
            }
        } else {
            titleBar.visibility = VISIBLE
            updatePinIconState()

            params.width = savedWidth
            params.height = savedHeight
            layoutParams = params

            animateToPosition(savedLeft, savedTop)
            animateToSize(savedWidth, savedHeight) {
                isMaximized = false
            }
        }
    }

    fun setFocusedState(focused: Boolean) {
        val targetElevation = if (focused) dpToPx(12).toFloat() else dpToPx(4).toFloat()
        elevation = targetElevation
        applyGlassStyle(focused)
    }

    private fun applyGlassStyle(focused: Boolean) {
        val glassBg = GradientDrawable().apply {
            setColor("#D0003554".toColorInt())
            val strokeColor = if (focused) {
                ContextCompat.getColor(context, R.color.accent)
            } else {
                "#33FFFFFF".toColorInt()
            }
            setStroke(dpToPx(if (focused) 1 else 1), strokeColor)
            cornerRadius = dpToPx(16).toFloat()
        }
        background = glassBg
    }

    private fun setResizingState(isResizing: Boolean) {
        if (isResizing) {
            alpha = 0.65f
            val resizingBg = GradientDrawable().apply {
                setColor("#44A8DFF4".toColorInt())
                setStroke(dpToPx(2), ContextCompat.getColor(context, R.color.accent))
                cornerRadius = dpToPx(16).toFloat()
            }
            background = resizingBg
        } else {
            alpha = 1.0f
            applyGlassStyle(true)
        }
    }

    private fun animateScale(view: View, scale: Float) {
        view.animate().scaleX(scale).scaleY(scale).setDuration(100).withEndAction {
            view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
        }.start()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
