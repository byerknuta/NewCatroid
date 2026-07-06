package org.catrobat.catroid.content.actions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.VideoView
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class NativeViewControlAction : TemporalAction() {
    var scope: Scope? = null
    var viewId: Formula? = null
    var parameter: Formula? = null
    var commandSelection: Int = 0

    private fun Formula?.interpretSafeBoolean(scope: Scope?): Boolean {
        if (this == null) return false
        try {
            val obj = this.interpretObject(scope)
            if (obj is Boolean) return obj
            if (obj is Number) return obj.toDouble() != 0.0

            val str = obj?.toString()?.trim()?.lowercase() ?: ""
            return str == "true" || str == "истина" || str == "да" || str == "yes" || str == "1"
        } catch (_: Exception) {
            return false
        }
    }

    override fun update(percent: Float) {
        val stage = StageActivity.activeStageActivity?.get() ?: return
        val idStr = viewId?.interpretString(scope) ?: return
        val paramStr = parameter?.interpretString(scope) ?: ""

        val view = stage.getViewFromStage(idStr) ?: return

        stage.runOnUiThread {
            try {
                when (commandSelection) {
                    0 -> if (view is WebView && view.canGoBack()) view.goBack()
                    1 -> if (view is WebView && view.canGoForward()) view.goForward()
                    2 -> if (view is WebView) view.reload()
                    3 -> if (view is WebView) view.stopLoading()
                    4 -> {
                        if (view is WebView) {
                            view.clearCache(true)
                            CookieManager.getInstance().removeAllCookies(null)
                        }
                    }
                    5 -> if (view is WebView) view.loadUrl(paramStr)
                    6 -> if (view is WebView) view.loadDataWithBaseURL("https://", paramStr, "text/html", "UTF-8", null)

                    7 -> if (view is VideoView) view.start()
                    8 -> if (view is VideoView && view.isPlaying) view.pause()
                    9 -> if (view is VideoView) view.stopPlayback()
                    10 -> {
                        if (view is VideoView) {
                            val seconds = paramStr.toDoubleOrNull() ?: 0.0
                            view.seekTo((seconds * 1000).toInt())
                        }
                    }

                    11 -> if (view is TextView) view.text = paramStr
                    12 -> if (view is TextView) view.append(paramStr)
                    13 -> if (view is TextView) view.text = ""
                    14 -> if (view is EditText) view.selectAll()
                    15 -> {
                        if (view is EditText) {
                            val index = paramStr.toIntOrNull() ?: 0
                            val clamped = 0.coerceAtLeast(view.text.length.coerceAtMost(index))
                            view.setSelection(clamped)
                        }
                    }

                    16 -> {
                        val parts = paramStr.split(",").map { it.trim().toIntOrNull() ?: 0 }
                        val sx = parts.getOrNull(0) ?: 0
                        val sy = parts.getOrNull(1) ?: 0
                        val vScroll = view.findViewWithTag<View>("scroll_content")?.parent as? View ?: view
                        (vScroll as? ScrollView)?.smoothScrollTo(sx, sy)
                    }
                    17 -> {
                        val parts = paramStr.split(",").map { it.trim().toIntOrNull() ?: 0 }
                        val dx = parts.getOrNull(0) ?: 0
                        val dy = parts.getOrNull(1) ?: 0
                        val vScroll = view.findViewWithTag<View>("scroll_content")?.parent as? View ?: view
                        (vScroll as? ScrollView)?.scrollBy(dx, dy)
                    }
                    18 -> {
                        val vScroll = view.findViewWithTag<View>("scroll_content")?.parent
                        if (vScroll is ScrollView) vScroll.fullScroll(ScrollView.FOCUS_UP)
                    }
                    19 -> {
                        val vScroll = view.findViewWithTag<View>("scroll_content")?.parent
                        if (vScroll is ScrollView) vScroll.fullScroll(ScrollView.FOCUS_DOWN)
                    }

                    20 -> {
                        view.requestFocus()
                        val imm = stage.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
                    }
                    21 -> {
                        view.clearFocus()
                        val imm = stage.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.hideSoftInputFromWindow(view.windowToken, 0)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun View.smoothScrollTo(x: Int, y: Int) {
        if (this is ScrollView) this.smoothScrollTo(x, y)
        else if (this is HorizontalScrollView) this.smoothScrollTo(x, y)
    }

    override fun restart() {
        super.restart()
    }
}
