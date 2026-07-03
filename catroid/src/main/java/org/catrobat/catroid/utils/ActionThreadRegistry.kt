package org.catrobat.catroid.utils

import com.badlogic.gdx.scenes.scene2d.Action
import java.util.concurrent.ConcurrentHashMap

object ActionThreadRegistry {
    private val activeThreads = ConcurrentHashMap<String, ArrayList<Action>>()

    @JvmStatic
    fun normalizeId(id: String): String {
        return id.trim().replace("\"", "").replace("'", "")
    }

    @JvmStatic
    fun register(threadId: String, action: Action) {
        val cleanId = normalizeId(threadId)
        val list = activeThreads.getOrPut(cleanId) { ArrayList() }
        list.add(action)
    }

    @JvmStatic
    fun unregister(threadId: String, action: Action) {
        val cleanId = normalizeId(threadId)
        val list = activeThreads[cleanId] ?: return
        list.remove(action)
        if (list.isEmpty()) {
            activeThreads.remove(cleanId)
        }
    }

    @JvmStatic
    fun isThreadRunning(threadId: String): Boolean {
        val cleanId = normalizeId(threadId)
        val list = activeThreads[cleanId]
        return list != null && list.isNotEmpty()
    }

    @JvmStatic
    fun stopThread(threadId: String) {
        val cleanId = normalizeId(threadId)
        val list = activeThreads[cleanId] ?: return
        for (action in list) {
            action.actor?.removeAction(action)
        }
        activeThreads.remove(cleanId)
    }

    @JvmStatic
    fun clear() {
        activeThreads.clear()
    }
}
