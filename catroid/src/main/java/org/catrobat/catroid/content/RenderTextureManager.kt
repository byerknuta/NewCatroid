package org.catrobat.catroid.content

import android.util.Log
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.ScreenUtils
import org.catrobat.catroid.ProjectManager
import org.catrobat.catroid.stage.StageActivity

class RenderTexture(val width: Int, val height: Int) {
    var fbo: FrameBuffer = FrameBuffer(Pixmap.Format.RGBA8888, width, height, true)
    var camera2D = OrthographicCamera(width.toFloat(), height.toFloat())
    var camera3D = PerspectiveCamera(67f, width.toFloat(), height.toFloat())
    var textureRegion: TextureRegion
    val spritesToRender = mutableListOf<Sprite>()

    var autoUpdate: Boolean = true
    var needsUpdate: Boolean = true

    var render2D: Boolean = true
    var render3D: Boolean = false

    init {
        textureRegion = TextureRegion(fbo.colorBufferTexture).apply { flip(false, true) }
        camera3D.near = 0.1f
        camera3D.far = 2500f
    }

    fun dispose() {
        fbo.dispose()
        spritesToRender.clear()
    }
}

object RenderTextureManager {
    val renderTextures = mutableMapOf<String, RenderTexture>()
    var isRenderingToBuffer: Boolean = false
        private set

    private const val GLOBAL_ROTATION_FIX = 90f

    private val tempMatrix = Matrix4()

    fun createRenderTarget(name: String, width: Int, height: Int) {
        val existing = renderTextures[name]
        if (existing != null && existing.width == width && existing.height == height) return

        Gdx.app.postRunnable {
            renderTextures[name]?.dispose()
            renderTextures[name] = RenderTexture(width, height)
        }
    }

    fun addSpriteToTarget(name: String, sprite: Sprite) {
        val target = renderTextures[name] ?: return
        if (!target.spritesToRender.contains(sprite)) {
            target.spritesToRender.add(sprite)
        }
    }

    fun removeSpriteFromTarget(name: String, sprite: Sprite) {
        renderTextures[name]?.spritesToRender?.remove(sprite)
    }

    fun setBufferMode(name: String, r2d: Boolean, r3d: Boolean) {
        renderTextures[name]?.let {
            it.render2D = r2d
            it.render3D = r3d
        }
    }

    fun setTargetCamera2D(name: String, x: Float, y: Float, zoom: Float, rotation: Float) {
        renderTextures[name]?.camera2D?.let {
            it.position.set(x, y, 0f)
            it.zoom = zoom
            it.up.set(0f, 1f, 0f)
            it.direction.set(0f, 0f, -1f)
            it.rotate(rotation + GLOBAL_ROTATION_FIX)
            it.update()
        }
    }

    fun setTargetCamera3D(name: String, x: Float, y: Float, z: Float, yaw: Float, pitch: Float, roll: Float, fov: Float) {
        renderTextures[name]?.camera3D?.let {
            it.position.set(x, y, z)
            it.fieldOfView = fov

            val rotation = com.badlogic.gdx.math.Quaternion().setEulerAngles(yaw, pitch, roll)
            it.direction.set(0f, 0f, -1f)
            rotation.transform(it.direction)
            it.up.set(0f, 1f, 0f)
            rotation.transform(it.up)

            it.update()
        }
    }

    fun setAutoUpdate(name: String, auto: Boolean) {
        renderTextures[name]?.autoUpdate = auto
    }

    fun saveBufferToFile(name: String, fileName: String) {
        Gdx.app.postRunnable {
            val target = renderTextures[name] ?: return@postRunnable

            target.fbo.begin()
            val pixels = ScreenUtils.getFrameBufferPixels(0, 0, target.width, target.height, true)
            target.fbo.end()

            Thread {
                val pixmap = Pixmap(target.width, target.height, Pixmap.Format.RGBA8888)
                val buffer = pixmap.pixels
                buffer.clear()
                buffer.put(pixels)
                buffer.position(0)

                val projectDir = ProjectManager.getInstance().currentProject.filesDir.absolutePath
                val file = Gdx.files.absolute("$projectDir/$fileName")

                PixmapIO.writePNG(file, pixmap)
                pixmap.dispose()
                Log.d("RenderTextureManager", "Сохранен скриншот буфера: ${file.path()}")
            }.start()
        }
    }

    fun getTextureRegion(name: String): TextureRegion? = renderTextures[name]?.textureRegion
    fun getWidth(name: String): Int = renderTextures[name]?.width ?: 0
    fun getHeight(name: String): Int = renderTextures[name]?.height ?: 0

    fun renderAllTargets(batch: Batch) {
        if (renderTextures.isEmpty()) return
        isRenderingToBuffer = true

        tempMatrix.set(batch.projectionMatrix)
        val wasDrawing = batch.isDrawing
        if (wasDrawing) batch.end()

        for ((_, target) in renderTextures) {
            if (!target.autoUpdate && !target.needsUpdate) continue

            if (target.render3D) {
                StageActivity.getActiveStageListener()?.threeDManager?.renderSceneForCustomCamera(target.camera3D, target.fbo)
            }

            if (target.render2D) {
                target.fbo.begin()
                if (!target.render3D) {
                    Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
                    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
                }

                target.camera2D.update()
                batch.projectionMatrix = target.camera2D.combined
                batch.begin()

                for (i in 0 until target.spritesToRender.size) {
                    target.spritesToRender[i].look?.draw(batch, 1.0f)
                }

                batch.end()
                target.fbo.end()
            }
            target.needsUpdate = false
        }

        batch.projectionMatrix = tempMatrix
        if (wasDrawing) batch.begin()
        isRenderingToBuffer = false
    }

    fun clearAll() {
        renderTextures.values.forEach { it.dispose() }
        renderTextures.clear()
    }
}
