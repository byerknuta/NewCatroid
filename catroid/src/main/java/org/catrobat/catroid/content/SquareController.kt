package org.catrobat.catroid.content

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import java.util.concurrent.ConcurrentHashMap

class SquareController private constructor() {
    private val squares: ConcurrentHashMap<String, SquareActor> = ConcurrentHashMap()

    companion object {
        @JvmStatic
        val instance: SquareController by lazy { SquareController() }
    }

    fun createOrUpdateSquare(
        name: String,
        x: Float, y: Float, width: Float, height: Float, colorHex: String,
        rotation: Float = 0f, transparency: Float = 1f, cornerRadius: Float = 0f,
        stage: Stage
    ) {
        val squareActor = SquareActor(x, y, width, height, colorHex, rotation, transparency, cornerRadius)
        val oldActor = squares.put(name, squareActor)

        Gdx.app.postRunnable {
            oldActor?.remove()
            stage.addActor(squareActor)
        }
    }

    fun removeSquare(name: String) {
        squares.remove(name)?.let { square ->
            Gdx.app.postRunnable {
                square.remove()
            }
        }
    }

    fun clearSquares() {
        val actorsToRemove = squares.values.toList()
        squares.clear()

        Gdx.app.postRunnable {
            actorsToRemove.forEach { it.remove() }
        }
    }
}
