package org.catrobat.catroid.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AlertDialog
import org.catrobat.catroid.R
import org.catrobat.catroid.formulaeditor.FormulaEditorEditText
import org.catrobat.catroid.formulaeditor.InternToken
import java.util.ArrayList

@SuppressLint("InflateParams")
object FormulaEditorClipboard {
    data class ClipboardItem(val tokens: List<InternToken>, val label: String)

    private val history = ArrayList<ClipboardItem>()
    private const val MAX_HISTORY_SIZE = 10

    private fun cloneTokens(tokens: List<InternToken>): List<InternToken> {
        val clonedTokens = ArrayList<InternToken>()
        tokens.forEach { token -> clonedTokens.add(token.deepCopy()) }
        return clonedTokens
    }

    fun checkIfSelectedAndCopy(editText: FormulaEditorEditText) {
        val tokens = editText.selectedTokens ?: editText.internFormula?.internTokenFormulaList

        if (tokens != null && tokens.isNotEmpty()) {
            val fullText = editText.stringFromInternFormula ?: ""

            val label = if (editText.selectedTokens != null) {
                val start = editText.internFormula?.externSelectionStartIndex ?: -1
                val end = editText.internFormula?.externSelectionEndIndex ?: -1

                if (start in 0..end && end <= fullText.length) {
                    val substring = fullText.substring(start, end).trim()
                    substring.ifEmpty { "Formula" }
                } else {
                    val singleTokenText = editText.selectedTextFromInternFormula
                    if (!singleTokenText.isNullOrEmpty()) singleTokenText else "Formula"
                }
            } else {
                fullText.trim().ifEmpty { "Formula" }
            }

            val cloned = cloneTokens(tokens)
            val item = ClipboardItem(cloned, label)

            history.removeAll { it.label == label }
            history.add(0, item)

            if (history.size > MAX_HISTORY_SIZE) {
                history.removeAt(history.size - 1)
            }
        }
    }

    fun paste(editText: FormulaEditorEditText) {
        if (history.isNotEmpty()) {
            editText.addTokens(cloneTokens(history[0].tokens))
        }
    }

    fun showClipboardHistoryDialog(context: Context, editText: FormulaEditorEditText) {
        if (history.isEmpty()) {
            AlertDialog.Builder(context)
                .setTitle(R.string.paste)
                .setMessage(R.string.formula_nothing_selected)
                .setPositiveButton(R.string.ok, null)
                .show()
            return
        }

        val items = history.map { it.label }.toTypedArray()
        AlertDialog.Builder(context)
            .setTitle(R.string.paste)
            .setItems(items) { _, which ->
                editText.addTokens(cloneTokens(history[which].tokens))
                val pastedItem = history.removeAt(which)
                history.add(0, pastedItem)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
