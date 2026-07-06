package org.catrobat.catroid.content.actions

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class CreateTextLabelAction : Action() {
    var scope: Scope? = null
    var viewId: Formula? = null
    var text: Formula? = null
    var colorHex: Formula? = null
    var fontSize: Formula? = null
    var bgColor: Formula? = null
    var fontPath: Formula? = null
    var cornerRadius: Formula? = null
    var lineSpacing: Formula? = null
    var formatMode: Int = 0
    var alignMode: Int = 0
    var scrollMode: Int = 0
    var x: Formula? = null
    var y: Formula? = null
    var width: Formula? = null
    var height: Formula? = null

    private var started = false
    @Volatile private var finished = false

    override fun act(delta: Float): Boolean {
        if (!started) {
            started = true
            runAsyncCreate()
        }
        return finished
    }

    private fun runAsyncCreate() {
        val stage = StageActivity.activeStageActivity?.get()
        if (stage == null) {
            finished = true
            return
        }

        val id = viewId?.interpretString(scope) ?: ""
        val rawText = text?.interpretString(scope) ?: ""
        val cHex = colorHex?.interpretString(scope) ?: "#FFFFFF"
        val size = fontSize?.interpretFloat(scope) ?: 20f
        val bgHex = bgColor?.interpretString(scope) ?: "#00000000"
        val font = fontPath?.interpretString(scope) ?: ""
        val radius = cornerRadius?.interpretInteger(scope) ?: 0
        val spacing = lineSpacing?.interpretFloat(scope) ?: 1.0f
        val px = x?.interpretInteger(scope) ?: 0
        val py = y?.interpretInteger(scope) ?: 0
        val w = width?.interpretInteger(scope) ?: 300
        val h = height?.interpretInteger(scope) ?: 100

        if (id.isEmpty()) {
            finished = true
            return
        }

        stage.runOnUiThread {
            try {
                val textView = TextView(stage)
                textView.textSize = size
                try {
                    textView.setTextColor(Color.parseColor(cHex))
                } catch (e: Exception) {
                    textView.setTextColor(Color.WHITE)
                }

                textView.setLineSpacing(0f, spacing)

                val backgroundShape = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = radius.toFloat()
                    try {
                        setColor(Color.parseColor(bgHex))
                    } catch (_: Exception) {
                        setColor(Color.TRANSPARENT)
                    }
                }
                textView.background = backgroundShape

                if (font.isNotEmpty()) {
                    val lowercaseFont = font.lowercase().trim()
                    textView.typeface = when (lowercaseFont) {
                        "sans", "sans-serif" -> Typeface.SANS_SERIF
                        "serif" -> Typeface.SERIF
                        "monospace" -> Typeface.MONOSPACE
                        "bold", "default-bold" -> Typeface.defaultFromStyle(Typeface.BOLD)
                        else -> {
                            val fontFile = scope?.project?.getFile(font)
                            if (fontFile != null && fontFile.exists()) {
                                try { Typeface.createFromFile(fontFile) } catch (e: Exception) { Typeface.DEFAULT }
                            } else {
                                Typeface.DEFAULT
                            }
                        }
                    }
                } else {
                    textView.typeface = Typeface.DEFAULT
                }

                textView.gravity = when (alignMode) {
                    1 -> Gravity.CENTER
                    2 -> Gravity.END or Gravity.CENTER_VERTICAL
                    else -> Gravity.START or Gravity.CENTER_VERTICAL
                }

                if (formatMode == 1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        textView.text = Html.fromHtml(rawText, Html.FROM_HTML_MODE_LEGACY)
                    } else {
                        @Suppress("DEPRECATION")
                        textView.text = Html.fromHtml(rawText)
                    }
                } else {
                    textView.text = rawText
                }

                when (scrollMode) {
                    1 -> {
                        textView.movementMethod = ScrollingMovementMethod()
                        textView.isVerticalScrollBarEnabled = true
                    }
                    2 -> {
                        textView.setSingleLine(true)
                        textView.movementMethod = ScrollingMovementMethod()
                        textView.isHorizontalScrollBarEnabled = true
                    }
                    3 -> {
                        textView.setSingleLine(false)
                        textView.setHorizontallyScrolling(true)
                        textView.movementMethod = ScrollingMovementMethod()
                    }
                }

                textView.visibility = org.catrobat.catroid.common.NativeViewBindingManager.defaultVisibility

                val params = FrameLayout.LayoutParams(w, h).apply {
                    leftMargin = px
                    topMargin = py
                }
                stage.addViewToStage(id, textView, params)
            } finally {
                finished = true
            }
        }
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
    }
}
