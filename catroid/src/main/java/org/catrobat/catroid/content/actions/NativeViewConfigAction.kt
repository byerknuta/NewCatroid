package org.catrobat.catroid.content.actions

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.widget.SwitchCompat
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

private class SyntaxConfig {
    val foregroundWords = mutableMapOf<String, Int>()
    val backgroundWords = mutableMapOf<String, Int>()
    var textWatcher: TextWatcher? = null
}

private fun applySyntaxHighlighting(editable: Editable, config: SyntaxConfig) {
    val oldForeSpans = editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)
    for (span in oldForeSpans) {
        editable.removeSpan(span)
    }
    val oldBackSpans = editable.getSpans(0, editable.length, BackgroundColorSpan::class.java)
    for (span in oldBackSpans) {
        editable.removeSpan(span)
    }

    for ((word, color) in config.foregroundWords) {
        var index = editable.indexOf(word)
        while (index >= 0) {
            val beforeChar = if (index > 0) editable[index - 1] else ' '
            val afterChar = if (index + word.length < editable.length) editable[index + word.length] else ' '
            val isWord = !beforeChar.isLetterOrDigit() && !afterChar.isLetterOrDigit()

            if (isWord) {
                editable.setSpan(
                    ForegroundColorSpan(color),
                    index,
                    index + word.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            index = editable.indexOf(word, index + word.length)
        }
    }

    for ((word, color) in config.backgroundWords) {
        var index = editable.indexOf(word)
        while (index >= 0) {
            val beforeChar = if (index > 0) editable[index - 1] else ' '
            val afterChar = if (index + word.length < editable.length) editable[index + word.length] else ' '
            val isWord = !beforeChar.isLetterOrDigit() && !afterChar.isLetterOrDigit()

            if (isWord) {
                editable.setSpan(
                    BackgroundColorSpan(color),
                    index,
                    index + word.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            index = editable.indexOf(word, index + word.length)
        }
    }
}

class NativeViewConfigAction : TemporalAction() {
    var scope: Scope? = null
    var viewId: Formula? = null
    var value: Formula? = null
    var propertySelection: Int = 0

    private fun Formula?.interpretSafeBoolean(scope: Scope?): Boolean {
        if (this == null) return false
        try {
            val obj = this.interpretObject(scope)
            if (obj is Boolean) return obj
            if (obj is Number) return obj.toDouble() != 0.0
            val str = obj?.toString()?.trim()?.lowercase() ?: ""
            return str == "true" || str == "истина" || str == "да" || str == "yes" || str == "1"
        } catch (e: Exception) {
            return false
        }
    }

    private fun boxBlur(src: android.graphics.Bitmap, range: Int): android.graphics.Bitmap {
        val width = src.width
        val height = src.height
        val pixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        val out = IntArray(width * height)
        val size = range * 2 + 1

        for (y in 0 until height) {
            val rowOffset = y * width
            var rSum = 0; var gSum = 0; var bSum = 0; var aSum = 0
            for (i in -range..range) {
                val pixel = pixels[rowOffset + Math.max(0, Math.min(width - 1, i))]
                rSum += (pixel shr 16) and 0xFF
                gSum += (pixel shr 8) and 0xFF
                bSum += pixel and 0xFF
                aSum += (pixel shr 24) and 0xFF
            }
            for (x in 0 until width) {
                out[rowOffset + x] = ((aSum / size) shl 24) or ((rSum / size) shl 16) or ((gSum / size) shl 8) or (bSum / size)
                val left = rowOffset + Math.max(0, x - range)
                val right = rowOffset + Math.min(width - 1, x + range + 1)
                val pLeft = pixels[left]
                val pRight = pixels[right]

                rSum += ((pRight shr 16) and 0xFF) - ((pLeft shr 16) and 0xFF)
                gSum += ((pRight shr 8) and 0xFF) - ((pLeft shr 8) and 0xFF)
                bSum += (pRight and 0xFF) - (pLeft and 0xFF)
                aSum += ((pRight shr 24) and 0xFF) - ((pLeft shr 24) and 0xFF)
            }
        }
        val finalPixels = IntArray(width * height)
        for (x in 0 until width) {
            var rSum = 0; var gSum = 0; var bSum = 0; var aSum = 0
            for (i in -range..range) {
                val pixel = out[Math.max(0, Math.min(height - 1, i)) * width + x]
                rSum += (pixel shr 16) and 0xFF
                gSum += (pixel shr 8) and 0xFF
                bSum += pixel and 0xFF
                aSum += (pixel shr 24) and 0xFF
            }
            for (y in 0 until height) {
                finalPixels[y * width + x] = ((aSum / size) shl 24) or ((rSum / size) shl 16) or ((gSum / size) shl 8) or (bSum / size)
                val top = Math.max(0, y - range) * width + x
                val bottom = Math.min(height - 1, y + range + 1) * width + x
                val pTop = out[top]
                val pBottom = out[bottom]

                rSum += ((pBottom shr 16) and 0xFF) - ((pTop shr 16) and 0xFF)
                gSum += ((pBottom shr 8) and 0xFF) - ((pTop shr 8) and 0xFF)
                bSum += (pBottom and 0xFF) - (pTop and 0xFF)
                aSum += ((pBottom shr 24) and 0xFF) - ((pTop shr 24) and 0xFF)
            }
        }

        val result = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        result.setPixels(finalPixels, 0, width, 0, 0, width, height)
        return result
    }

    override fun update(percent: Float) {
        val stage = StageActivity.activeStageActivity?.get() ?: return
        val idStr = viewId?.interpretString(scope) ?: return
        val valStr = value?.interpretString(scope) ?: ""
        val valBool = value?.interpretBoolean(scope) ?: false

        val view = stage.getViewFromStage(idStr) ?: return

        stage.runOnUiThread {
            try {
                when (propertySelection) {
                    0 -> {
                        view.visibility = if (valBool) View.VISIBLE else View.GONE
                    }
                    1 -> {
                        view.alpha = valStr.toFloatOrNull() ?: 1.0f
                    }
                    2 -> {
                        view.isClickable = valBool
                        view.isFocusable = valBool
                    }
                    3 -> {
                        val toBackground = valStr.toFloatOrNull() ?: 0.0f
                        val parent = view.parent as? ViewGroup
                        if (parent != null) {
                            view.elevation = toBackground
                        }
                    }
                    4 -> {
                        view.rotation = valStr.toFloatOrNull() ?: 0.0f
                    }
                    5 -> {
                        view.scaleX = valStr.toFloatOrNull() ?: 1.0f
                    }
                    6 -> {
                        view.scaleY = valStr.toFloatOrNull() ?: 1.0f
                    }
                    7 -> {
                        if (view is WebView) {
                            CookieManager.getInstance().setAcceptCookie(valBool)
                        }
                    }
                    8 -> {
                        if (view is WebView) {
                            view.settings.builtInZoomControls = valBool
                        }
                    }
                    9 -> {
                        if (view is WebView) {
                            view.settings.domStorageEnabled = valBool
                        }
                    }
                    10 -> {
                        if (view is WebView) {
                            view.settings.userAgentString = valStr
                        }
                    }
                    11 -> {
                        if (view is WebView) {
                            try {
                                view.setBackgroundColor(Color.parseColor(valStr))
                            } catch (_: Exception) {
                                view.setBackgroundColor(Color.TRANSPARENT)
                            }
                        }
                    }
                    12 -> {
                        if (view is VideoView) {
                            val field = VideoView::class.java.getDeclaredField("mMediaPlayer")
                            field.isAccessible = true
                            val mp = field.get(view) as? android.media.MediaPlayer
                            val vol = valStr.toFloatOrNull() ?: 1.0f
                            mp?.setVolume(vol, vol)
                        }
                    }
                    13 -> {
                        if (view is VideoView) {
                            val field = VideoView::class.java.getDeclaredField("mMediaPlayer")
                            field.isAccessible = true
                            val mp = field.get(view) as? android.media.MediaPlayer
                            mp?.isLooping = valBool
                        }
                    }
                    14 -> {
                        if (view is TextView) {
                            view.setTextColor(Color.parseColor(valStr))
                        }
                    }
                    15 -> {
                        if (view is TextView) {
                            view.textSize = valStr.toFloatOrNull() ?: 20f
                        }
                    }
                    16 -> {
                        if (view is TextView) {
                            val bg = view.background
                            if (bg is GradientDrawable) {
                                bg.setColor(Color.parseColor(valStr))
                            } else {
                                view.setBackgroundColor(Color.parseColor(valStr))
                            }
                        }
                    }
                    17 -> {
                        val radius = valStr.toFloatOrNull() ?: 0.0f
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (radius > 0) {
                                view.outlineProvider = object : ViewOutlineProvider() {
                                    override fun getOutline(v: View, outline: Outline) {
                                        outline.setRoundRect(0, 0, v.width, v.height, radius)
                                    }
                                }
                                view.clipToOutline = true
                            } else {
                                view.clipToOutline = false
                                view.outlineProvider = null
                            }
                        }
                    }
                    18 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            view.elevation = valStr.toFloatOrNull() ?: 0.0f
                        }
                    }
                    19 -> {
                        if (view is TextView) {
                            val align = valStr.toIntOrNull() ?: 0
                            view.gravity = when (align) {
                                1 -> Gravity.CENTER
                                2 -> Gravity.END or Gravity.CENTER_VERTICAL
                                else -> Gravity.START or Gravity.CENTER_VERTICAL
                            }
                        }
                    }
                    20 -> {
                        if (view is TextView) {
                            val font = valStr.lowercase().trim()
                            view.typeface = when (font) {
                                "sans", "sans-serif" -> Typeface.SANS_SERIF
                                "serif" -> Typeface.SERIF
                                "monospace" -> Typeface.MONOSPACE
                                "bold", "default-bold" -> Typeface.defaultFromStyle(Typeface.BOLD)
                                else -> {
                                    val fontFile = scope?.project?.getFile(valStr)
                                    if (fontFile != null && fontFile.exists()) {
                                        try { Typeface.createFromFile(fontFile) } catch (e: Exception) { Typeface.DEFAULT }
                                    } else {
                                        Typeface.DEFAULT
                                    }
                                }
                            }
                        }
                    }
                    21 -> {
                        if (view is TextView) {
                            view.setLineSpacing(0f, valStr.toFloatOrNull() ?: 1.0f)
                        }
                    }
                    22 -> {
                        if (view is EditText) {
                            val limit = valStr.toIntOrNull() ?: -1
                            if (limit > 0) {
                                view.filters = arrayOf(InputFilter.LengthFilter(limit))
                            }
                        }
                    }
                    23 -> {
                        if (view is EditText) {
                            view.hint = valStr
                        }
                    }
                    24 -> {
                        if (view is EditText) {
                            view.setHintTextColor(Color.parseColor(valStr))
                        }
                    }
                    25 -> {
                        if (view is SwitchCompat) {
                            view.text = valStr
                        }
                    }
                    26 -> {
                        if (view is SwitchCompat) {
                            view.isChecked = valBool
                        }
                    }
                    27 -> {
                        if (view is Button) {
                            view.isAllCaps = valBool
                        }
                    }
                    28 -> {
                        if (view is ImageView) {
                            val scale = valStr.toIntOrNull() ?: 0
                            view.scaleType = when (scale) {
                                1 -> ImageView.ScaleType.CENTER_CROP
                                2 -> ImageView.ScaleType.FIT_XY
                                3 -> ImageView.ScaleType.CENTER
                                else -> ImageView.ScaleType.FIT_CENTER
                            }
                        }
                    }
                    29 -> {
                        if (view is ImageView) {
                            if (valStr.startsWith("http://") || valStr.startsWith("https://")) {
                                thread(start = true) {
                                    try {
                                        val connection = URL(valStr).openConnection() as HttpURLConnection
                                        connection.doInput = true
                                        connection.connect()
                                        val bitmap = BitmapFactory.decodeStream(connection.inputStream)
                                        stage.runOnUiThread { view.setImageBitmap(bitmap) }
                                    } catch (e: Exception) { e.printStackTrace() }
                                }
                            } else {
                                val file = scope?.project?.getFile(valStr)
                                if (file != null && file.exists()) {
                                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                    view.setImageBitmap(bitmap)
                                }
                            }
                        }
                    }
                    30 -> {
                        if (view is SeekBar) {
                            val range = view.tag as? Pair<Float, Float> ?: Pair(0f, 100f)
                            val newMin = valStr.toFloatOrNull() ?: 0f
                            view.tag = Pair(newMin, range.second)
                        }
                    }
                    31 -> {
                        if (view is SeekBar) {
                            val range = view.tag as? Pair<Float, Float> ?: Pair(0f, 100f)
                            val newMax = valStr.toFloatOrNull() ?: 100f
                            view.tag = Pair(range.first, newMax)
                        }
                    }
                    32 -> {
                        if (view is SeekBar) {
                            val range = view.tag as? Pair<Float, Float> ?: Pair(0f, 100f)
                            val min = range.first
                            val max = range.second
                            val current = valStr.toFloatOrNull() ?: min
                            val progress = (((current - min) / (max - min)) * 1000).toInt()
                            view.progress = Math.max(0, Math.min(1000, progress))
                        }
                    }
                    33 -> {
                        val scroll = view.findViewWithTag<View>("scroll_content")?.parent
                        if (scroll is ScrollView) scroll.isVerticalScrollBarEnabled = valBool
                        if (scroll is HorizontalScrollView) scroll.isHorizontalScrollBarEnabled = valBool
                    }
                    34 -> {
                        val overScrollMode = valStr.toIntOrNull() ?: 1
                        val overScrollVal = when (overScrollMode) {
                            0 -> View.OVER_SCROLL_ALWAYS
                            2 -> View.OVER_SCROLL_NEVER
                            else -> View.OVER_SCROLL_IF_CONTENT_SCROLLS
                        }
                        val scroll = view.findViewWithTag<View>("scroll_content")?.parent as? View
                        scroll?.overScrollMode = overScrollVal
                    }
                    35 -> {
                        val padding = valStr.toIntOrNull() ?: 0
                        val content = view.findViewWithTag<View>("scroll_content")
                        content?.setPadding(padding, padding, padding, padding)
                    }
                    36 -> {
                        val parts = valStr.split(",").map { it.trim() }
                        val strokeWidth = parts.getOrNull(0)?.toIntOrNull() ?: 0
                        val strokeColorHex = parts.getOrNull(1) ?: "#FFFFFF"

                        val bg = view.background as? GradientDrawable ?: GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            setColor(Color.TRANSPARENT)
                        }
                        try {
                            bg.setStroke(strokeWidth, Color.parseColor(strokeColorHex))
                            view.background = bg
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    37 -> {
                        val p = valStr.toIntOrNull() ?: 0
                        view.setPadding(p, p, p, p)
                    }
                    38 -> {
                        val blurRadius = valStr.toFloatOrNull() ?: 0.0f
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (blurRadius > 0) {
                                view.setRenderEffect(android.graphics.RenderEffect.createBlurEffect(blurRadius, blurRadius, android.graphics.Shader.TileMode.CLAMP))
                            } else {
                                view.setRenderEffect(null)
                            }
                        } else {
                            if (blurRadius > 0) {
                                if (view is ImageView) {
                                    val drawable = view.drawable
                                    if (drawable is android.graphics.drawable.BitmapDrawable) {
                                        val originalBitmap = drawable.bitmap
                                        if (originalBitmap != null) {
                                            if (view.getTag(org.catrobat.catroid.R.id.brick_layout) == null) {
                                                view.setTag(org.catrobat.catroid.R.id.brick_layout, originalBitmap)
                                            }

                                            val scaleFactor = 4
                                            val scaledW = Math.max(8, originalBitmap.width / scaleFactor)
                                            val scaledH = Math.max(8, originalBitmap.height / scaleFactor)

                                            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, scaledW, scaledH, false)
                                            val calculatedRadius = Math.max(1, (blurRadius / scaleFactor).toInt())

                                            val blurredBitmap = boxBlur(scaledBitmap, calculatedRadius)
                                            view.setImageBitmap(blurredBitmap)
                                        }
                                    }
                                } else {
                                    if (view.getTag(org.catrobat.catroid.R.id.brick_layout) == null) {
                                        view.setTag(org.catrobat.catroid.R.id.brick_layout, view.background)
                                    }

                                    val bg = view.background
                                    if (bg is GradientDrawable) {
                                        bg.setColor(Color.parseColor("#A0E0E0E0"))
                                    } else {
                                        view.setBackgroundColor(Color.parseColor("#A0E0E0E0"))
                                    }
                                }
                            } else {
                                if (view is ImageView) {
                                    val originalBitmap = view.getTag(org.catrobat.catroid.R.id.brick_layout) as? android.graphics.Bitmap
                                    if (originalBitmap != null) {
                                        view.setImageBitmap(originalBitmap)
                                        view.setTag(org.catrobat.catroid.R.id.brick_layout, null)
                                    }
                                } else {
                                    val cachedBg = view.getTag(org.catrobat.catroid.R.id.brick_layout)
                                    if (cachedBg != null || view.background != null) {
                                        val originalBg = cachedBg as? android.graphics.drawable.Drawable
                                        view.background = originalBg
                                        view.setTag(org.catrobat.catroid.R.id.brick_layout, null)
                                    }
                                }
                            }
                        }
                    }
                    39 -> {
                        val parts = valStr.split(",").map { it.trim().toFloatOrNull() }
                        val px = parts.getOrNull(0) ?: (view.width / 2f)
                        val py = parts.getOrNull(1) ?: (view.height / 2f)
                        view.pivotX = px
                        view.pivotY = py
                    }
                    40 -> {
                        val isBg = valStr.lowercase() == "background" || valStr.lowercase() == "задний"
                        val parent = view.parent as? ViewGroup
                        parent?.removeView(view)

                        val layerName = if (isBg) "backgroundLayout" else "foregroundLayout"
                        val field = StageActivity::class.java.getDeclaredField(layerName)
                        field.isAccessible = true
                        val targetLayout = field.get(stage) as? ViewGroup

                        targetLayout?.addView(view, view.layoutParams)

                        if (view is android.view.SurfaceView) {
                            if (isBg) {
                                view.setZOrderOnTop(false)
                            } else {
                                view.setZOrderOnTop(true)
                            }
                        }
                    }
                    41 -> {
                        val isVisible = valStr.lowercase() == "true" || valStr.lowercase() == "истина" || valStr == "1" || valBool
                        org.catrobat.catroid.common.NativeViewBindingManager.defaultVisibility = if (isVisible) View.VISIBLE else View.GONE
                    }
                    42 -> {
                        if (view is WebView) {
                            val scale = valStr.toFloatOrNull() ?: 100f
                            view.setInitialScale(scale.toInt())
                        }
                    }
                    43 -> {
                        if (view is TextView) {
                            val parts = valStr.split(",").map { it.trim() }
                            val start = parts.getOrNull(0)?.toIntOrNull() ?: 0
                            val end = parts.getOrNull(1)?.toIntOrNull() ?: 0
                            val colorHex = parts.getOrNull(2) ?: "#FFFFFF"

                            val spannable = android.text.SpannableString(view.text)
                            val clampedStart = 0.coerceAtLeast(view.text.length.coerceAtMost(start))
                            val clampedEnd = 0.coerceAtLeast(view.text.length.coerceAtMost(end))

                            if (clampedStart < clampedEnd) {
                                try {
                                    val color = Color.parseColor(colorHex)
                                    spannable.setSpan(
                                        ForegroundColorSpan(color),
                                        clampedStart,
                                        clampedEnd,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )

                                    if (view is EditText) {
                                        val selectionStart = view.selectionStart
                                        val selectionEnd = view.selectionEnd
                                        view.setText(spannable, TextView.BufferType.SPANNABLE)
                                        val length = view.text.length
                                        view.setSelection(selectionStart.coerceAtMost(length), selectionEnd.coerceAtMost(length))
                                    } else {
                                        view.text = spannable
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                    44 -> {
                        if (view is TextView) {
                            val parts = valStr.split(",").map { it.trim() }
                            val wordToHighlight = parts.getOrNull(0) ?: ""
                            val colorHex = parts.getOrNull(1) ?: "#FFFFFF"

                            if (wordToHighlight.isNotEmpty()) {
                                val color = try { Color.parseColor(colorHex) } catch (e: Exception) { Color.YELLOW }

                                val config = (view.tag as? SyntaxConfig) ?: SyntaxConfig().also { view.tag = it }

                                config.foregroundWords[wordToHighlight] = color

                                if (config.textWatcher == null) {
                                    var isUpdating = false
                                    val watcher = object : TextWatcher {
                                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                                        override fun afterTextChanged(s: Editable?) {
                                            if (isUpdating || s == null) return
                                            isUpdating = true
                                            if (view is EditText) {
                                                val selectionStart = view.selectionStart
                                                val selectionEnd = view.selectionEnd
                                                applySyntaxHighlighting(s, config)
                                                val length = view.text.length
                                                view.setSelection(selectionStart.coerceAtMost(length), selectionEnd.coerceAtMost(length))
                                            } else {
                                                applySyntaxHighlighting(s, config)
                                            }
                                            isUpdating = false
                                        }
                                    }
                                    config.textWatcher = watcher
                                    view.addTextChangedListener(watcher)
                                }

                                if (view is EditText) {
                                    view.text?.let { applySyntaxHighlighting(it, config) }
                                } else {
                                    val editable = Editable.Factory.getInstance().newEditable(view.text)
                                    applySyntaxHighlighting(editable, config)
                                    view.text = editable
                                }
                            }
                        }
                    }
                    45 -> {
                        if (view is TextView) {
                            val parts = valStr.split(",").map { it.trim() }
                            val start = parts.getOrNull(0)?.toIntOrNull() ?: 0
                            val end = parts.getOrNull(1)?.toIntOrNull() ?: 0
                            val colorHex = parts.getOrNull(2) ?: "#FFFF00"

                            val spannable = android.text.SpannableString(view.text)
                            val clampedStart = 0.coerceAtLeast(view.text.length.coerceAtMost(start))
                            val clampedEnd = 0.coerceAtLeast(view.text.length.coerceAtMost(end))

                            if (clampedStart < clampedEnd) {
                                try {
                                    val color = Color.parseColor(colorHex)
                                    spannable.setSpan(
                                        BackgroundColorSpan(color),
                                        clampedStart,
                                        clampedEnd,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )

                                    if (view is EditText) {
                                        val selectionStart = view.selectionStart
                                        val selectionEnd = view.selectionEnd
                                        view.setText(spannable, TextView.BufferType.SPANNABLE)
                                        val length = view.text.length
                                        view.setSelection(selectionStart.coerceAtMost(length), selectionEnd.coerceAtMost(length))
                                    } else {
                                        view.text = spannable
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                    46 -> {
                        if (view is TextView) {
                            val parts = valStr.split(",").map { it.trim() }
                            val wordToHighlight = parts.getOrNull(0) ?: ""
                            val colorHex = parts.getOrNull(1) ?: "#FFFF00"

                            if (wordToHighlight.isNotEmpty()) {
                                val color = try { Color.parseColor(colorHex) } catch (e: Exception) { Color.YELLOW }

                                val config = (view.tag as? SyntaxConfig) ?: SyntaxConfig().also { view.tag = it }

                                config.backgroundWords[wordToHighlight] = color

                                if (config.textWatcher == null) {
                                    var isUpdating = false
                                    val watcher = object : TextWatcher {
                                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                                        override fun afterTextChanged(s: Editable?) {
                                            if (isUpdating || s == null) return
                                            isUpdating = true
                                            if (view is EditText) {
                                                val selectionStart = view.selectionStart
                                                val selectionEnd = view.selectionEnd
                                                applySyntaxHighlighting(s, config)
                                                val length = view.text.length
                                                view.setSelection(selectionStart.coerceAtMost(length), selectionEnd.coerceAtMost(length))
                                            } else {
                                                applySyntaxHighlighting(s, config)
                                            }
                                            isUpdating = false
                                        }
                                    }
                                    config.textWatcher = watcher
                                    view.addTextChangedListener(watcher)
                                }

                                if (view is EditText) {
                                    view.text?.let { applySyntaxHighlighting(it, config) }
                                } else {
                                    val editable = Editable.Factory.getInstance().newEditable(view.text)
                                    applySyntaxHighlighting(editable, config)
                                    view.text = editable
                                }
                            }
                        }
                    }
                    47 -> {
                        org.catrobat.catroid.utils.OverlayViewManager.setViewAsOverlay(idStr, valBool)
                    }
                    48 -> {
                        org.catrobat.catroid.utils.OverlayViewManager.setViewDraggable(idStr, valBool)
                    }
                    49 -> {
                        org.catrobat.catroid.utils.OverlayViewManager.setDragHandle(idStr, valStr)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun restart() {
        super.restart()
    }
}
