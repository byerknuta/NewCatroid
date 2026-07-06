package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.formulaeditor.UserVariable
import org.catrobat.catroid.stage.StageActivity

class CreateTextFieldAction : Action() {
    var scope: Scope? = null
    var name: Formula? = null
    var variable: UserVariable? = null
    var posX: Formula? = null
    var posY: Formula? = null
    var width: Formula? = null
    var height: Formula? = null
    var initialText: Formula? = null

    var text_size: Formula? = null
    var text_color: Formula? = null
    var bg_color: Formula? = null
    var hint_text: Formula? = null
    var hint_color: Formula? = null
    var alignment_f: Formula? = null
    var font_path: Formula? = null
    var input_type: Formula? = null
    var is_password: Formula? = null
    var max_length: Formula? = null
    var corner_radius: Formula? = null

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
        val activity = StageActivity.activeStageActivity?.get()
        if (activity == null) {
            finished = true
            return
        }

        val nameT = name?.interpretString(scope) ?: ""
        val posXT = posX?.interpretInteger(scope) ?: 0
        val posYT = posY?.interpretInteger(scope) ?: 0
        val widthT = width?.interpretInteger(scope) ?: 0
        val heightT = height?.interpretInteger(scope) ?: 0
        val initText = initialText?.interpretString(scope) ?: ""

        if (nameT.isEmpty()) {
            finished = true
            return
        }

        val customStyles = HashMap<String, String>()
        customStyles[StageActivity.STYLE_TEXT_SIZE] = text_size?.interpretString(scope) ?: "22"
        customStyles[StageActivity.STYLE_TEXT_COLOR] = text_color?.interpretString(scope) ?: "#FFFFFF"
        customStyles[StageActivity.STYLE_BACKGROUND_COLOR] = bg_color?.interpretString(scope) ?: "#88000000"
        customStyles[StageActivity.STYLE_HINT_TEXT] = hint_text?.interpretString(scope) ?: "Enter value..."
        customStyles[StageActivity.STYLE_HINT_TEXT_COLOR] = hint_color?.interpretString(scope) ?: "#CCCCCC"
        customStyles[StageActivity.STYLE_TEXT_ALIGNMENT] = alignment_f?.interpretString(scope) ?: "left"
        font_path?.interpretString(scope)?.let {
            customStyles[StageActivity.STYLE_FONT_PATH] = scope!!.project?.getFile(it)?.absolutePath ?: it
        }
        input_type?.interpretString(scope)?.let { customStyles[StageActivity.STYLE_INPUT_TYPE] = it }
        max_length?.interpretString(scope)?.let { customStyles[StageActivity.STYLE_MAX_LENGTH] = it }
        corner_radius?.interpretString(scope)?.let { customStyles[StageActivity.STYLE_CORNER_RADIUS] = it }
        val isPasswordValue = is_password?.interpretBoolean(scope) ?: false
        customStyles[StageActivity.STYLE_IS_PASSWORD] = isPasswordValue.toString()

        activity.runOnUiThread {
            try {
                activity.createInputField(
                    nameT,
                    variable,
                    initText,
                    posXT, posYT, widthT, heightT,
                    customStyles
                )
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
