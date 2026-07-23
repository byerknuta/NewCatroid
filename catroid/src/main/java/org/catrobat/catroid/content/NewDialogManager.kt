package org.catrobat.catroid.content

import androidx.appcompat.app.AlertDialog
import android.graphics.Color
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import com.danvexteam.lunoscript_annotations.LunoClass
import org.catrobat.catroid.R

@LunoClass
class NewDialogManager {
    companion object {
        private val dialogMap = mutableMapOf<String, DialogData>()

        fun createEmptyDialog(name: String, title: String, message: String) {
            dialogMap[name] = DialogData(title, message)
        }

        fun setPositiveButton(name: String, buttonText: String) {
            dialogMap[name]?.positiveButtonText = buttonText
        }

        fun setNegativeButton(name: String, buttonText: String) {
            dialogMap[name]?.negativeButtonText = buttonText
        }

        fun setNeutralButton(name: String, buttonText: String) {
            dialogMap[name]?.neutralButtonText = buttonText
        }

        fun addEditText(name: String, defaultValue: String = "") {
            dialogMap[name]?.inputs?.add(defaultValue)
        }

        fun addRadio(name: String, option: String) {
            dialogMap[name]?.radioOptions?.add(option)
        }

        fun setCallback(name: String, callback: (result: String) -> Unit) {
            dialogMap[name]?.callback = callback
        }

        fun showDialog(name: String) {
            val context = MyActivityManager.stage_activity ?: return
            val dialogData = dialogMap[name] ?: return

            context.runOnUiThread {
                val container = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    val padding = resources.getDimensionPixelSize(R.dimen.dialog_content_area_padding)
                    setPadding(padding, padding, padding, padding)
                }

                val editTextFields = mutableListOf<EditText>()
                var radioGroup: RadioGroup? = null

                for (defaultValue in dialogData.inputs) {
                    val editText = androidx.appcompat.widget.AppCompatEditText(context).apply {
                        setText(defaultValue)
                        setTextColor(Color.WHITE)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = resources.getDimensionPixelSize(R.dimen.material_design_spacing_small)
                            bottomMargin = resources.getDimensionPixelSize(R.dimen.material_design_spacing_small)
                        }
                    }
                    editTextFields.add(editText)
                    container.addView(editText)
                }

                if (dialogData.radioOptions.isNotEmpty()) {
                    radioGroup = RadioGroup(context).apply {
                        layoutParams = RadioGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.VERTICAL
                    }
                    for (option in dialogData.radioOptions) {
                        val radioButton = RadioButton(context).apply {
                            text = option
                            setTextColor(Color.WHITE)
                        }
                        radioGroup.addView(radioButton)
                    }
                    container.addView(radioGroup)
                }

                fun getEnteredValue(): String {
                    val sb = StringBuilder()
                    if (radioGroup != null) {
                        val checkedId = radioGroup!!.checkedRadioButtonId
                        if (checkedId != -1) {
                            val rb = radioGroup!!.findViewById<RadioButton>(checkedId)
                            sb.append(rb.text).append("\n")
                        } else {
                            sb.append("\n")
                        }
                    }
                    for (et in editTextFields) {
                        sb.append(et.text.toString()).append("\n")
                    }
                    return sb.toString().trim()
                }

                val builder = AlertDialog.Builder(context)
                    .setTitle(dialogData.title)
                    .setMessage(dialogData.message)
                    .setCancelable(false)
                    .setView(container)

                dialogData.positiveButtonText?.let { btnText ->
                    builder.setPositiveButton(btnText) { dialog, _ ->
                        dialog.dismiss()
                        dialogData.callback?.invoke("1\n${getEnteredValue()}")
                    }
                }

                dialogData.negativeButtonText?.let { btnText ->
                    builder.setNegativeButton(btnText) { dialog, _ ->
                        dialog.dismiss()
                        dialogData.callback?.invoke("-1\n${getEnteredValue()}")
                    }
                }

                dialogData.neutralButtonText?.let { btnText ->
                    builder.setNeutralButton(btnText) { dialog, _ ->
                        dialog.dismiss()
                        dialogData.callback?.invoke("0\n${getEnteredValue()}")
                    }
                }

                builder.create().show()
            }
        }

        private class DialogData(var title: String, var message: String) {
            val inputs = mutableListOf<String>()
            val radioOptions = mutableListOf<String>()
            var positiveButtonText: String? = null
            var negativeButtonText: String? = null
            var neutralButtonText: String? = null
            var callback: ((result: String) -> Unit)? = null
        }
    }
}
