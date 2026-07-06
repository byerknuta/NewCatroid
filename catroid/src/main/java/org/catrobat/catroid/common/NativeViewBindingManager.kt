package org.catrobat.catroid.common

import android.view.View
import android.widget.FrameLayout
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.Viewport
import org.catrobat.catroid.content.Sprite
import org.catrobat.catroid.stage.StageActivity
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import kotlin.math.abs

object NativeViewBindingManager {

    @JvmStatic
    var defaultVisibility: Int = View.VISIBLE

    class Binding(
        val viewId: String,
        val sprite: Sprite,
        val offsetX: Float,
        val offsetY: Float,
        val alignMode: Int
    )

    private val activeBindings = ConcurrentHashMap<String, Binding>()

    @JvmStatic
    fun bind(viewId: String, sprite: Sprite, offsetX: Float, offsetY: Float, alignMode: Int) {
        activeBindings[viewId] = Binding(viewId, sprite, offsetX, offsetY, alignMode)
    }

    @JvmStatic
    fun unbind(viewId: String) {
        activeBindings.remove(viewId)
    }

    @JvmStatic
    fun clearAll() {
        activeBindings.clear()
    }

    @JvmStatic
    fun updateBindings(camera: Camera, viewport: Viewport) {
        if (activeBindings.isEmpty()) return

        val stage = StageActivity.activeStageActivity?.get() ?: return

        stage.runOnUiThread {
            val pCenter = Vector3()
            val pEdge = Vector3()

            for (binding in activeBindings.values) {
                val view = stage.getViewFromStage(binding.viewId) ?: continue
                val sprite = binding.sprite
                val look = sprite.look ?: continue

                if (!look.isVisible) {
                    view.visibility = View.GONE
                    continue
                } else {
                    view.visibility = View.VISIBLE
                }

                val worldCenterX = look.x + look.width / 2f
                val worldCenterY = look.y + look.height / 2f

                val virtualW = look.width * look.scaleX
                val virtualH = look.height * look.scaleY

                pCenter.set(worldCenterX, worldCenterY, 0f)
                viewport.project(pCenter)

                pEdge.set(worldCenterX + virtualW / 2f, worldCenterY + virtualH / 2f, 0f)
                viewport.project(pEdge)

                val androidCenterX = pCenter.x
                val androidCenterY = Gdx.graphics.height - pCenter.y

                val screenW = abs(pEdge.x - pCenter.x) * 2f
                val screenH = abs(pEdge.y - pCenter.y) * 2f

                val finalX = androidCenterX - (screenW / 2f) + binding.offsetX
                val finalY = androidCenterY - (screenH / 2f) + binding.offsetY

                val params = view.layoutParams as? FrameLayout.LayoutParams
                if (params != null) {
                    params.width = screenW.toInt()
                    params.height = screenH.toInt()
                    params.leftMargin = finalX.toInt()
                    params.topMargin = finalY.toInt()
                    view.layoutParams = params
                }


                view.rotation = -look.rotation
                view.alpha = look.color.a
            }
        }
    }
}
