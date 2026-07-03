package org.catrobat.catroid.ai

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.catrobat.catroid.content.Script
import org.catrobat.catroid.content.Sprite
import org.catrobat.catroid.content.bricks.Brick
import org.catrobat.catroid.ui.recyclerview.adapter.BrickAdapter
import kotlinx.coroutines.*

class KoveAutocompleteController private constructor() {

    companion object {
        private var instance: KoveAutocompleteController? = null

        @JvmStatic
        fun getInstance(): KoveAutocompleteController {
            if (instance == null) {
                instance = KoveAutocompleteController()
            }
            return instance!!
        }
    }

    private var activeSprite: Sprite? = null
    private var activeScript: Script? = null
    private var adapter: BrickAdapter? = null
    private var selectedIndex: Int = -1

    private var autocompleteJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val currentPhantomBricks = mutableListOf<Brick>()

    fun onUserActivity(sprite: Sprite, script: Script, brickIndex: Int, adapter: BrickAdapter) {
        cancelActiveJob()

        this.activeSprite = sprite
        this.activeScript = script
        this.adapter = adapter
        this.selectedIndex = brickIndex

        if (currentPhantomBricks.isNotEmpty()) {
            dismissSuggestions()
        }

        autocompleteJob = scope.launch {
            delay(2500)
            triggerInference()
        }
    }

    private suspend fun triggerInference() {
        val sprite = activeSprite ?: return
        val script = activeScript ?: return
        val currentAdapter = adapter ?: return

        try {
            val (prefix, suffix) = withContext(Dispatchers.Default) {
                KoveContextGenerator.generateContext(sprite, script, selectedIndex)
            }

            Log.d("KOVE_DEBUG", "==================== KOVE INPUT PREFIX ====================\n$prefix")
            Log.d("KOVE_DEBUG", "==================== KOVE INPUT SUFFIX ====================\n$suffix")

            val resultRaw = withContext(Dispatchers.Default) {
                KoveManager.getAutocompleteSuggestion(prefix, suffix)
            }

            // Очищаем отладочные хвосты и FIM-теги
            val resultDsl = cleanResponse(resultRaw)

            // [ЛОГИРОВАНИЕ СЫРОГО ВЫВОДА МОДЕЛИ]
            Log.d("KOVE_DEBUG", "==================== KOVE RAW OUTPUT ====================\n$resultDsl")

            if (resultDsl.isEmpty() || resultDsl.startsWith("ERROR")) return

            val newBricks = withContext(Dispatchers.Default) {
                val lexer = KoveLexer(resultDsl)
                val tokens = lexer.tokenize()
                val parser = KoveParser(tokens)
                val parsedBricks = parser.parseBricks()

                KoveModelConverter.buildBricks(parsedBricks, sprite)
            }

            if (newBricks.isEmpty()) return

            newBricks.forEach { it.setPhantom(true) }

            val insertPos = selectedIndex + 1
            script.brickList.addAll(insertPos, newBricks)
            currentPhantomBricks.addAll(newBricks)

            currentAdapter.updateItems(sprite)
            Log.i("KOVE_AUTOCOMPLETE", "Inserted ${newBricks.size} phantom bricks at index $insertPos")

        } catch (e: Exception) {
            Log.e("KOVE_AUTOCOMPLETE", "Error in autocomplete pipeline", e)
        }
    }

    fun acceptSuggestions() {
        val sprite = activeSprite ?: return
        if (currentPhantomBricks.isEmpty()) return

        currentPhantomBricks.forEach { it.setPhantom(false) }
        currentPhantomBricks.clear()

        adapter?.updateItems(sprite)
        cancelActiveJob()
        Log.i("KOVE_AUTOCOMPLETE", "Suggestions accepted and materialized.")
    }

    fun dismissSuggestions() {
        val sprite = activeSprite ?: return
        val script = activeScript ?: return
        if (currentPhantomBricks.isEmpty()) return

        script.brickList.removeAll(currentPhantomBricks)
        currentPhantomBricks.clear()

        adapter?.updateItems(sprite)
        Log.i("KOVE_AUTOCOMPLETE", "Suggestions dismissed and removed.")
    }

    fun cancelActiveJob() {
        autocompleteJob?.cancel()
        autocompleteJob = null
    }

    private fun cleanResponse(raw: String): String {
        val stopTokens = listOf(
            "<|fim_prefix|>",
            "<|fim_suffix|>",
            "<|fim_middle|>",
            "<|endoftext|>",
            "<|file_sep|>"
        )
        var clean = raw
        for (token in stopTokens) {
            if (clean.contains(token)) {
                clean = clean.substringBefore(token)
            }
        }
        return clean.trim()
    }
}
