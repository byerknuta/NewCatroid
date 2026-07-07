package org.catrobat.catroid.utils

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import org.catrobat.catroid.CatroidApplication
import org.catrobat.catroid.stage.StageActivity
import java.util.concurrent.ConcurrentHashMap

object OverlayViewManager {
    private val overlayViews = ConcurrentHashMap<String, View>()

    @JvmStatic
    fun setViewAsOverlay(viewId: String, isOverlay: Boolean) {
        val context = CatroidApplication.getAppContext()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val stage = StageActivity.activeStageActivity?.get() ?: return

        stage.runOnUiThread {
            try {
                val view = stage.getViewFromStage(viewId) ?: return@runOnUiThread

                if (isOverlay) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        return@runOnUiThread
                    }

                    if (overlayViews.containsKey(viewId)) return@runOnUiThread

                    val parent = view.parent as? ViewGroup
                    parent?.removeView(view)

                    val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        @Suppress("DEPRECATION")
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                    }

                    val oldParams = view.layoutParams as? FrameLayout.LayoutParams
                    val w = oldParams?.width ?: WindowManager.LayoutParams.WRAP_CONTENT
                    val h = oldParams?.height ?: WindowManager.LayoutParams.WRAP_CONTENT
                    val xPos = oldParams?.leftMargin ?: 0
                    val yPos = oldParams?.topMargin ?: 0

                    val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH

                    val params = WindowManager.LayoutParams(
                        w, h,
                        layoutType,
                        flags,
                        PixelFormat.TRANSLUCENT
                    ).apply {
                        gravity = Gravity.TOP or Gravity.START
                        x = xPos
                        y = yPos
                    }

                    view.setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_OUTSIDE) {
                            if ((params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) == 0) {
                                params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                windowManager.updateViewLayout(view, params)
                            }
                        } else {
                            if ((params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) != 0) {
                                params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                                windowManager.updateViewLayout(view, params)
                            }
                        }
                        false
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                        view.setOnApplyWindowInsetsListener { v, insets ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                val isVisible = insets.isVisible(android.view.WindowInsets.Type.ime())
                                StageActivity.isKeyboardVisible = isVisible

                                if (isVisible) {
                                    val h = insets.getInsets(android.view.WindowInsets.Type.ime()).bottom
                                    if (h > 200 && h > StageActivity.realKeyboardHeight) {
                                        StageActivity.realKeyboardHeight = h
                                    }
                                }
                            } else {
                                val h = insets.systemWindowInsetBottom
                                if (h > 100) {
                                    StageActivity.isKeyboardVisible = true
                                    if (h > 200 && h > StageActivity.realKeyboardHeight) {
                                        StageActivity.realKeyboardHeight = h
                                    }
                                } else if (h < 50) {
                                    StageActivity.isKeyboardVisible = false
                                }
                            }
                            v.onApplyWindowInsets(insets)
                        }
                    }

                    windowManager.addView(view, params)
                    overlayViews[viewId] = view
                } else {
                    val viewInOverlay = overlayViews.remove(viewId) ?: return@runOnUiThread

                    try {
                        windowManager.removeView(viewInOverlay)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val defaultParams = FrameLayout.LayoutParams(viewInOverlay.width, viewInOverlay.height).apply {
                        gravity = Gravity.TOP or Gravity.START
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                        viewInOverlay.setOnApplyWindowInsetsListener(null)
                    }
                    viewInOverlay.setOnTouchListener(null)

                    stage.addViewToStage(viewId, viewInOverlay, defaultParams)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun updateOverlayPosition(viewId: String, xPos: Int, yPos: Int) {
        val view = overlayViews[viewId] ?: return
        val context = CatroidApplication.getAppContext()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val stage = StageActivity.activeStageActivity?.get() ?: return

        stage.runOnUiThread {
            try {
                val params = view.layoutParams as? WindowManager.LayoutParams ?: return@runOnUiThread
                params.x = xPos
                params.y = yPos
                windowManager.updateViewLayout(view, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun removeAllOverlays() {
        val context = CatroidApplication.getAppContext()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        for ((_, view) in overlayViews) {
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayViews.clear()
    }

    @JvmStatic
    fun setDragHandle(handleViewId: String, targetOverlayId: String) {
        val stage = StageActivity.activeStageActivity?.get() ?: return
        stage.runOnUiThread {
            val handleView = stage.getViewFromStage(handleViewId) ?: return@runOnUiThread
            val targetView = overlayViews[targetOverlayId] ?: return@runOnUiThread
            val windowManager = CatroidApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager

            var initialX = 0
            var initialY = 0
            var initialTouchX = 0f
            var initialTouchY = 0f
            var isMoving = false

            handleView.setOnTouchListener { _, event ->
                val params = targetView.layoutParams as? WindowManager.LayoutParams ?: return@setOnTouchListener false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isMoving = false
                        false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()

                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isMoving = true
                            params.x = initialX + dx
                            params.y = initialY + dy
                            windowManager.updateViewLayout(targetView, params)
                            true
                        } else {
                            false
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        isMoving
                    }
                    else -> false
                }
            }
        }
    }

    @JvmStatic
    fun setViewDraggable(viewId: String, draggable: Boolean) {
        val view = overlayViews[viewId] ?: return
        val context = CatroidApplication.getAppContext()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val stage = StageActivity.activeStageActivity?.get() ?: return

        stage.runOnUiThread {
            if (draggable) {
                var initialX = 0
                var initialY = 0
                var initialTouchX = 0f
                var initialTouchY = 0f
                var isMoving = false

                view.setOnTouchListener { v, event ->
                    val params = v.layoutParams as? WindowManager.LayoutParams ?: return@setOnTouchListener false

                    val focusParams = v.layoutParams as? WindowManager.LayoutParams
                    if (focusParams != null && event.action != MotionEvent.ACTION_OUTSIDE) {
                        if ((focusParams.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) != 0) {
                            focusParams.flags = focusParams.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                            windowManager.updateViewLayout(v, focusParams)
                        }
                    }

                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            isMoving = false
                            false
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val dx = (event.rawX - initialTouchX).toInt()
                            val dy = (event.rawY - initialTouchY).toInt()

                            if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                                isMoving = true
                                params.x = initialX + dx
                                params.y = initialY + dy
                                windowManager.updateViewLayout(v, params)
                                true
                            } else {
                                false
                            }
                        }
                        MotionEvent.ACTION_UP -> {
                            isMoving
                        }
                        MotionEvent.ACTION_OUTSIDE -> {
                            if (focusParams != null && (focusParams.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) == 0) {
                                focusParams.flags = focusParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                windowManager.updateViewLayout(v, focusParams)
                            }
                            false
                        }
                        else -> false
                    }
                }
            } else {
                view.setOnTouchListener { v, event ->
                    val params = v.layoutParams as? WindowManager.LayoutParams ?: return@setOnTouchListener false
                    if (event.action == MotionEvent.ACTION_OUTSIDE) {
                        if ((params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) == 0) {
                            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            windowManager.updateViewLayout(v, params)
                        }
                    } else {
                        if ((params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) != 0) {
                            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                            windowManager.updateViewLayout(v, params)
                        }
                    }
                    false
                }
            }
        }
    }
}
