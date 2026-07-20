package org.catrobat.catroid.content

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Actor

class SquareActor(
    private val x: Float,
    private val y: Float,
    private val width: Float,
    private val height: Float,
    colorHex: String,
    private val rotation: Float = 0f,
    transparency: Float = 1f,
    private val cornerRadius: Float = 0f
) : Actor() {

    private val color: Color = Color.valueOf(colorHex).apply { a = transparency }

    companion object {
        private var sharedShapeRenderer: ShapeRenderer? = null

        fun getShapeRenderer(): ShapeRenderer {
            if (sharedShapeRenderer == null) {
                sharedShapeRenderer = ShapeRenderer()
            }
            return sharedShapeRenderer!!
        }

        @JvmStatic
        fun disposeShared() {
            sharedShapeRenderer?.dispose()
            sharedShapeRenderer = null
        }
    }

    init {
        setBounds(x, y, width, height)
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        if (batch == null) return

        batch.end()

        val sr = getShapeRenderer()
        sr.projectionMatrix = batch.projectionMatrix
        sr.transformMatrix = batch.transformMatrix

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        sr.begin(ShapeRenderer.ShapeType.Filled)
        sr.color = Color(color.r, color.g, color.b, color.a * parentAlpha)

        sr.identity()
        sr.translate(x + width / 2, y + height / 2, 0f)
        sr.rotate(0f, 0f, 1f, rotation)

        if (cornerRadius > 0) {
            drawRoundedRectangle(sr, -width / 2, -height / 2, width, height, cornerRadius)
        } else {
            sr.rect(-width / 2, -height / 2, width, height)
        }

        sr.end()

        Gdx.gl.glDisable(GL20.GL_BLEND)

        batch.begin()
    }

    private fun drawRoundedRectangle(
        shapeRenderer: ShapeRenderer,
        rx: Float, ry: Float, rWidth: Float, rHeight: Float, radius: Float
    ) {
        shapeRenderer.rect(rx + radius, ry, rWidth - 2 * radius, rHeight)
        shapeRenderer.rect(rx, ry + radius, rWidth, rHeight - 2 * radius)

        shapeRenderer.arc(rx + radius, ry + radius, radius, 180f, 90f)
        shapeRenderer.arc(rx + rWidth - radius, ry + radius, radius, 270f, 90f)
        shapeRenderer.arc(rx + radius, ry + rHeight - radius, radius, 90f, 90f)
        shapeRenderer.arc(rx + rWidth - radius, ry + rHeight - radius, radius, 0f, 90f)
    }
}
