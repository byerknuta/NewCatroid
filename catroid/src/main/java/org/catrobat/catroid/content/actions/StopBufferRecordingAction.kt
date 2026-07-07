package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.utils.BufferVideoRecorder
import kotlin.concurrent.thread

class StopBufferRecordingAction : Action() {
    var scope: Scope? = null

    private var started = false
    @Volatile private var finished = false

    override fun act(delta: Float): Boolean {
        if (!started) {
            started = true
            thread(start = true, name = "StopVideoMuxer") {
                try {
                    BufferVideoRecorder.stopRecordingAndWait()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    finished = true
                }
            }
        }
        return finished
    }

    override fun restart() {
        super.restart()
        started = false
        finished = false
    }

    override fun reset() {
        super.reset()
        started = false
        finished = false
        scope = null
    }
}
