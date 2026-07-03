package org.catrobat.catroid.content.actions

import android.util.Log
import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.formulaeditor.InterpretationException
import org.catrobat.catroid.formulaeditor.UserVariable
import org.catrobat.catroid.stage.ShowTextActor
import org.catrobat.catroid.stage.StageActivity
import org.catrobat.catroid.utils.ShowTextUtils
import org.catrobat.catroid.utils.ShowTextUtils.AndroidStringProvider

class ShowTextAction : Action() {
    companion object {
        val TAG: String = ShowTextAction::class.java.simpleName
    }

    private var xPosition: Formula = Formula(0)
    private var yPosition: Formula = Formula(0)
    private var variableToShow: UserVariable? = null

    private var scope: Scope? = null
    private var androidStringProvider: AndroidStringProvider? = null

    override fun act(delta: Float): Boolean {
        try {
            val variable = variableToShow ?: return true
            variable.visible = true

            val xPos = xPosition.interpretFloat(scope)
            val yPos = yPosition.interpretFloat(scope)

            val listener = StageActivity.activeStageActivity.get()?.stageListener ?: return true
            val stageActors = listener.stage?.actors ?: return true

            var targetActor: ShowTextActor? = null

            for (i in 0 until stageActors.size) {
                val stageActor = stageActors.get(i)
                if (stageActor is ShowTextActor) {
                    if (stageActor.variableNameToCompare == variable.name && stageActor.sprite == scope?.sprite) {
                        targetActor = stageActor
                        break
                    }
                }
            }

            if (targetActor == null) {
                targetActor = ShowTextActor(
                    false, variable, "", xPos, yPos, 1.0f, null,
                    scope?.sprite, ShowTextUtils.ALIGNMENT_STYLE_CENTERED, androidStringProvider
                )
                listener.addActor(targetActor)
            } else {
                targetActor.updateProperties(variable.name, xPos, yPos, 1.0f, null, null)
            }
        } catch (e: InterpretationException) {
            Log.d(TAG, "InterpretationException: $e")
        }
        return true
    }

    fun setPosition(xPosition: Formula, yPosition: Formula) {
        this.xPosition = xPosition
        this.yPosition = yPosition
    }

    fun setScope(scope: Scope?) {
        this.scope = scope
    }

    fun setVariableToShow(userVariable: UserVariable?) {
        variableToShow = userVariable
    }

    fun setAndroidStringProvider(androidStringProvider: AndroidStringProvider?) {
        this.androidStringProvider = androidStringProvider
    }
}
