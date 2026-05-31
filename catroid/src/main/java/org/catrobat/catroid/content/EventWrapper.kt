package org.catrobat.catroid.content

import com.badlogic.gdx.scenes.scene2d.Event
import org.catrobat.catroid.content.eventids.EventId

class EventWrapper(internal val eventId: EventId, private val wait: Boolean) : Event() {
    private var spriteWaitList: MutableMap<Sprite, Int?>? = null

    fun notify(sprite: Sprite) {
        spriteWaitList?.let {
            if (it.containsKey(sprite)) {
                it[sprite] = it[sprite]?.minus(1)
            }
            if (it[sprite] == 0) {
                it.remove(sprite)
            }
        }
    }

    internal fun addSpriteToWaitList(sprite: Sprite) = wait.also {
        if (it) {
            if (spriteWaitList == null) {
                spriteWaitList = mutableMapOf()
            }
            val list = spriteWaitList!!
            list[sprite] = (list[sprite] ?: 0) + 1
        }
    }

    fun isWaitingForSprite() = spriteWaitList?.isNotEmpty() == true
}
