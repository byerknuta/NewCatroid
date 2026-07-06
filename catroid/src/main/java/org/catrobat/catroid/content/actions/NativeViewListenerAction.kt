package org.catrobat.catroid.content.actions

import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.widget.SwitchCompat
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.formulaeditor.UserVariable
import org.catrobat.catroid.stage.StageActivity

class NativeViewListenerAction : TemporalAction() {
    var scope: Scope? = null
    var viewId: Formula? = null
    var userVariable: UserVariable? = null
    var eventSelection: Int = 0

    override fun update(percent: Float) {
        val stage = StageActivity.activeStageActivity?.get() ?: return
        val idStr = viewId?.interpretString(scope) ?: return
        val variable = userVariable ?: return

        val view = stage.getViewFromStage(idStr) ?: return

        stage.runOnUiThread {
            try {
                val TRUE_VAL = true
                val FALSE_VAL = false

                when (eventSelection) {
                    0 -> {
                        if (view is WebView) {
                            view.addJavascriptInterface(object : Any() {
                                @android.webkit.JavascriptInterface
                                fun postMessage(message: String) {
                                    stage.runOnUiThread {
                                        variable.value = message
                                    }
                                }
                            }, "Android")
                        }
                    }
                    1, 2, 3 -> {
                        if (view is WebView) {
                            view.webViewClient = object : WebViewClient() {
                                override fun onPageStarted(v: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                    if (eventSelection == 1) variable.value = url ?: ""
                                }

                                override fun onPageFinished(v: WebView?, url: String?) {
                                    if (eventSelection == 2) variable.value = url ?: ""
                                }

                                override fun onReceivedError(v: WebView?, req: WebResourceRequest?, err: WebResourceError?) {
                                    if (eventSelection == 3) {
                                        variable.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            err?.description?.toString() ?: "Error"
                                        } else {
                                            "Error"
                                        }
                                    }
                                }
                            }
                        }
                    }
                    4, 5 -> {
                        if (view is WebView) {
                            view.webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(v: WebView?, newProgress: Int) {
                                    if (eventSelection == 4) variable.value = newProgress.toDouble()
                                }

                                override fun onReceivedTitle(v: WebView?, title: String?) {
                                    if (eventSelection == 5) variable.value = title ?: ""
                                }
                            }
                        }
                    }

                    6 -> {
                        if (view is TextView) {
                            view.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                    variable.value = s?.toString() ?: ""
                                }
                                override fun afterTextChanged(s: Editable?) {}
                            })
                        }
                    }
                    7, 8 -> {
                        if (view is TextView) {
                            view.setOnFocusChangeListener { _, hasFocus ->
                                if (eventSelection == 7 && hasFocus) {
                                    variable.value = TRUE_VAL
                                } else if (eventSelection == 8 && !hasFocus) {
                                    variable.value = FALSE_VAL
                                }
                            }
                        }
                    }
                    9 -> {
                        if (view is TextView) {
                            view.setOnEditorActionListener { _, _, _ ->
                                variable.value = view.text.toString()
                                false
                            }
                        }
                    }

                    10 -> {
                        if (view is VideoView) {
                            view.setOnCompletionListener {
                                variable.value = TRUE_VAL
                            }
                        }
                    }
                    11 -> {
                        if (view is VideoView) {
                            view.setOnPreparedListener {
                                variable.value = TRUE_VAL
                            }
                        }
                    }
                    12 -> {
                        if (view is VideoView) {
                            view.setOnErrorListener { _, what, extra ->
                                variable.value = "Error $what ($extra)"
                                true
                            }
                        }
                    }

                    13, 14, 15 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (view is ScrollView || view is HorizontalScrollView) {
                                view.setOnScrollChangeListener { _, scrollX, scrollY, _, _ ->
                                    if (eventSelection == 13) {
                                        variable.value = scrollY.toDouble()
                                    } else if (eventSelection == 14) {
                                        variable.value = scrollX.toDouble()
                                    } else if (eventSelection == 15 && view is ScrollView) {
                                        val child = view.getChildAt(0)
                                        val diff = child.bottom - (view.height + view.scrollY)
                                        if (diff <= 0) {
                                            variable.value = TRUE_VAL
                                        }
                                    }
                                }
                            }
                        }
                    }

                    16 -> {
                        view.setOnClickListener {
                            variable.value = TRUE_VAL
                        }
                    }
                    17 -> {
                        if (view is SeekBar) {
                            view.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                                    val range = sb?.tag as? Pair<Float, Float> ?: Pair(0f, 100f)
                                    val min = range.first
                                    val max = range.second
                                    val value = min + (progress / 1000f) * (max - min)
                                    variable.value = value.toDouble()
                                }
                                override fun onStartTrackingTouch(sb: SeekBar?) {}
                                override fun onStopTrackingTouch(sb: SeekBar?) {}
                            })
                        }
                    }
                    18 -> {
                        if (view is SwitchCompat) {
                            view.setOnCheckedChangeListener { _, isChecked ->
                                variable.value = if (isChecked) TRUE_VAL else FALSE_VAL
                            }
                        }
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
