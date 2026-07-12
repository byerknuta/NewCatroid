package org.catrobat.catroid.content.actions

import android.util.Log
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.formulaeditor.UserVariable
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.JsePlatform
import java.util.concurrent.ConcurrentHashMap

class RunLuaAction() : TemporalAction() {
    var scope: Scope? = null
    var code: Formula? = null
    var userVariable: UserVariable? = null

    private var isRunning = false
    private var isFinished = false

    companion object {
        private val sharedGlobals: Globals by lazy { JsePlatform.standardGlobals() }

        private val compiledChunks = ConcurrentHashMap<String, LuaValue>()
    }

    override fun act(delta: Float): Boolean {
        if (!isRunning) {
            isRunning = true
            val codeStr = code?.interpretString(scope) ?: "return 0"

            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val chunk: LuaValue = compiledChunks[codeStr] ?: sharedGlobals.load(codeStr).also { compiledChunks[codeStr] = it }

                    val result: LuaValue = chunk.call()
                    Gdx.app.postRunnable {
                        userVariable?.value = result.tojstring()
                        isFinished = true
                    }
                } catch (e: Throwable) {
                    Log.e("RunLuaAction", "FATAL LUA ERROR DURING GENERATION: ", e)
                    Gdx.app.postRunnable {
                        isFinished = true
                    }
                }
            }
        }

        return isFinished
    }

    override fun update(percent: Float) {
    }
}
