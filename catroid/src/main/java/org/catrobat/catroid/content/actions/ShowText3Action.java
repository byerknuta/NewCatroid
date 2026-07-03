package org.catrobat.catroid.content.actions;

import android.util.Log;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

import org.catrobat.catroid.content.Scope;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.InterpretationException;
import org.catrobat.catroid.formulaeditor.UserVariable;
import org.catrobat.catroid.stage.ShowTextActor;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.utils.ShowTextUtils.AndroidStringProvider;

public class ShowText3Action extends Action {
    private Formula xPosition, yPosition, relativeTextSize, color, text, name;
    private Scope scope;
    private int alignment;
    private AndroidStringProvider androidStringProvider;

    @Override
    public boolean act(float delta) {
        try {
            String nameStr = name.interpretString(scope) != null ? name.interpretString(scope) : "dummyActor";
            String textStr = text.interpretString(scope) != null ? text.interpretString(scope) : "NaN";
            float xPos = xPosition.interpretFloat(scope);
            float yPos = yPosition.interpretFloat(scope);
            float relSize = relativeTextSize.interpretFloat(scope) / 100f;
            String colStr = color.interpretString(scope);

            if (StageActivity.getActiveStageListener() == null) return true;

            Array<Actor> stageActors = StageActivity.getActiveStageListener().getStage().getActors();
            ShowTextActor targetActor = null;

            for (Actor a : stageActors) {
                if (a instanceof ShowTextActor) {
                    ShowTextActor sta = (ShowTextActor) a;
                    if (sta.getVariableNameToCompare().equals(nameStr) && sta.getSprite().equals(scope.getSprite())) {
                        targetActor = sta;
                        break;
                    }
                }
            }

            if (targetActor == null) {
                UserVariable variableToShow = new UserVariable(nameStr, textStr);
                targetActor = new ShowTextActor(true, variableToShow, textStr, xPos, yPos, relSize, colStr, scope.getSprite(), alignment, androidStringProvider);
                StageActivity.getActiveStageListener().addActor(targetActor);
                variableToShow.setVisible(true);
            } else {
                targetActor.updateProperties(textStr, xPos, yPos, relSize, colStr, null);
            }
        } catch (InterpretationException e) {
            Log.d("ShowText3Action", "InterpretationException: " + e);
        }
        return true;
    }

    public void setPosition(Formula xPosition, Formula yPosition) { this.xPosition = xPosition; this.yPosition = yPosition; }
    public void setRelativeTextSize(Formula relativeTextSize) { this.relativeTextSize = relativeTextSize; }
    public void setColor(Formula color) { this.color = color; }
    public void setScope(Scope scope) { this.scope = scope; }
    public void setText(Formula text) { this.text = text; }
    public void setName(Formula name) { this.name = name; }
    public void setAlignment(int alignment) { this.alignment = alignment; }
    public void setAndroidStringProvider(AndroidStringProvider androidStringProvider) { this.androidStringProvider = androidStringProvider; }
}
