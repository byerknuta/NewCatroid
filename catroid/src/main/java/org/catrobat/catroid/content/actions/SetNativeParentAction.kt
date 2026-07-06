package org.catrobat.catroid.content.actions

import android.view.ViewGroup
import android.widget.FrameLayout
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class SetNativeParentAction : TemporalAction() {
    var scope: Scope? = null
    var viewId: Formula? = null
    var parentId: Formula? = null

    override fun update(percent: Float) {
        val stage = StageActivity.activeStageActivity?.get() ?: return
        val vId = viewId?.interpretString(scope) ?: return
        val pId = parentId?.interpretString(scope) ?: ""

        val childView = stage.getViewFromStage(vId) ?: return

        stage.runOnUiThread {
            (childView.parent as? ViewGroup)?.removeView(childView)

            if (pId.isEmpty()) {
                val params = childView.layoutParams as? FrameLayout.LayoutParams
                    ?: FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                stage.addViewToStage(vId, childView, params)
            } else {
                val parentView = stage.getViewFromStage(pId)
                if (parentView is ViewGroup) {
                    val innerContainer = parentView.findViewWithTag<ViewGroup>("scroll_content") ?: parentView
                    innerContainer.addView(childView, childView.layoutParams)
                }
            }
        }
    }

    override fun restart() {
        super.restart()
    }
}
