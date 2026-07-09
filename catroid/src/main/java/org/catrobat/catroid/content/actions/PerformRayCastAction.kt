package org.catrobat.catroid.content.actions

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.ProjectManager
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula

class PerformRayCastAction : TemporalAction() {
    private var scope: Scope? = null
    private var rayId: Formula? = null
    private var startX: Formula? = null
    private var startY: Formula? = null
    private var endX: Formula? = null
    private var endY: Formula? = null

    override fun update(percent: Float) {
        val id = rayId?.interpretString(scope)
        if (id.isNullOrEmpty()) return

        val sX = startX?.interpretFloat(scope) ?: return
        val sY = startY?.interpretFloat(scope) ?: return
        val eX = endX?.interpretFloat(scope) ?: return
        val eY = endY?.interpretFloat(scope) ?: return

        if (!sX.isFinite() || !sY.isFinite() || !eX.isFinite() || !eY.isFinite()) {
            return
        }

        val dx = eX - sX
        val dy = eY - sY
        if (dx * dx + dy * dy < 0.0001f) {
            return
        }

        val limit = 50000f
        val clampedStart = Vector2(sX.coerceIn(-limit, limit), sY.coerceIn(-limit, limit))
        val clampedEnd = Vector2(eX.coerceIn(-limit, limit), eY.coerceIn(-limit, limit))

        val scene = ProjectManager.getInstance().currentlyPlayingScene ?: return

        try {
            scene.physicsWorld.performRayCast(id, clampedStart, clampedEnd)
        } catch (e: Exception) {
            android.util.Log.e("PerformRayCast", "Failed to perform RayCast in native Box2D", e)
        }
    }

    fun setScope(scope: Scope?) {
        this.scope = scope
    }

    fun setRayId(rayId: Formula?) {
        this.rayId = rayId
    }

    fun setStartX(startX: Formula?) {
        this.startX = startX
    }

    fun setStartY(startY: Formula?) {
        this.startY = startY
    }

    fun setEndX(endX: Formula?) {
        this.endX = endX
    }

    fun setEndY(endY: Formula?) {
        this.endY = endY
    }
}
