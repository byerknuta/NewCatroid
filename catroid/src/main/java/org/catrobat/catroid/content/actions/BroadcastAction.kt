package org.catrobat.catroid.content.actions

import org.catrobat.catroid.content.eventids.BroadcastEventId

class BroadcastAction : MultiSpriteEventAction() {
    private var cachedEventId: BroadcastEventId? = null

    var broadcastMessage: String? = null
        set(value) {
            field = value
            cachedEventId = null
        }

    override fun getEventId(): BroadcastEventId? {
        if (cachedEventId == null && broadcastMessage != null) {
            cachedEventId = BroadcastEventId(broadcastMessage)
        }
        return cachedEventId
    }
}
