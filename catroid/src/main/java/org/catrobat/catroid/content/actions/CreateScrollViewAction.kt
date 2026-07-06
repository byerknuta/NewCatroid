package org.catrobat.catroid.content.actions

import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class CreateScrollViewAction : TemporalAction() {
    var scope: Scope? = null
    var viewId: Formula? = null
    var scrollMode: Int = 1 // 0=None, 1=Vert, 2=Horiz, 3=Both
    var bgColor: Formula? = null
    var showBars: Formula? = null
    var padding: Formula? = null
    var overScrollMode: Int = 1 // 0=Always, 1=IfScrolls, 2=Never
    var x: Formula? = null
    var y: Formula? = null
    var width: Formula? = null
    var height: Formula? = null

    override fun update(percent: Float) {
        val stage = StageActivity.activeStageActivity?.get() ?: return
        val id = viewId?.interpretString(scope) ?: return
        val bgHex = bgColor?.interpretString(scope) ?: "#00000000"
        val showScrollBars = showBars?.interpretBoolean(scope) ?: true
        val innerPadding = padding?.interpretInteger(scope) ?: 0
        val px = x?.interpretInteger(scope) ?: 0
        val py = y?.interpretInteger(scope) ?: 0
        val w = width?.interpretInteger(scope) ?: 300
        val h = height?.interpretInteger(scope) ?: 300

        stage.runOnUiThread {
            val wrapper = FrameLayout(stage)
            try {
                wrapper.setBackgroundColor(Color.parseColor(bgHex))
            } catch (e: Exception) {
                wrapper.setBackgroundColor(Color.TRANSPARENT)
            }

            val contentLayer = FrameLayout(stage)
            contentLayer.tag = "scroll_content"
            contentLayer.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            contentLayer.setPadding(innerPadding, innerPadding, innerPadding, innerPadding)

            var finalView: View = contentLayer

            val overScrollVal = when (overScrollMode) {
                0 -> View.OVER_SCROLL_ALWAYS
                2 -> View.OVER_SCROLL_NEVER
                else -> View.OVER_SCROLL_IF_CONTENT_SCROLLS
            }

            if (scrollMode == 2 || scrollMode == 3) {
                val hScroll = HorizontalScrollView(stage)
                hScroll.isFillViewport = true
                hScroll.isHorizontalScrollBarEnabled = showScrollBars
                hScroll.overScrollMode = overScrollVal
                hScroll.addView(finalView)
                finalView = hScroll
            }
            if (scrollMode == 1 || scrollMode == 3) {
                val vScroll = ScrollView(stage)
                vScroll.isFillViewport = true
                vScroll.isVerticalScrollBarEnabled = showScrollBars
                vScroll.overScrollMode = overScrollVal
                vScroll.addView(finalView)
                finalView = vScroll
            }

            wrapper.addView(finalView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            wrapper.visibility = org.catrobat.catroid.common.NativeViewBindingManager.defaultVisibility
            val params = FrameLayout.LayoutParams(w, h).apply {
                leftMargin = px
                topMargin = py
            }
            stage.addViewToStage(id, wrapper, params)
        }
    }

    override fun restart() {
        super.restart()
    }
}
