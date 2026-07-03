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

public class ShowText2Action extends Action {
    private Formula name, xPosition, yPosition, relativeTextSize, color, text;
    private Scope scope;
    private int alignment;
    private AndroidStringProvider androidStringProvider;

    @Override
    public boolean act(float delta) {
        try {
            float xPos = xPosition.interpretFloat(scope);
            float yPos = yPosition.interpretFloat(scope);
            String actorName = name.interpretString(scope);
            String textStr = text.interpretString(scope);
            float relSize = relativeTextSize.interpretFloat(scope) / 100f;
            String colStr = color.interpretString(scope);

            if (StageActivity.getActiveStageListener() == null) return true;

            Array<Actor> stageActors = StageActivity.getActiveStageListener().getStage().getActors();
            ShowTextActor targetActor = null;

            for (Actor a : stageActors) {
                if (a instanceof ShowTextActor) {
                    ShowTextActor sta = (ShowTextActor) a;
                    if (sta.getVariableNameToCompare().equals(actorName) && sta.getSprite().equals(scope.getSprite())) {
                        targetActor = sta;
                        break;
                    }
                }
            }

            if (targetActor == null) {
                UserVariable vari = new UserVariable(actorName, textStr);
                targetActor = new ShowTextActor(false, vari, textStr, xPos, yPos, relSize, colStr, scope.getSprite(), alignment, androidStringProvider);
                StageActivity.getActiveStageListener().addActor(targetActor);
                vari.setVisible(true);
            } else {
                targetActor.updateProperties(textStr, xPos, yPos, relSize, colStr, null);
            }
        } catch (InterpretationException e) {
            Log.d("ShowText2Action", "InterpretationException: " + e);
        }
        return true;
    }

    public void setPosition(Formula xPosition, Formula yPosition) { this.xPosition = xPosition; this.yPosition = yPosition; }
    public void setRelativeTextSize(Formula relativeTextSize) { this.relativeTextSize = relativeTextSize; }
    public void setColor(Formula color) { this.color = color; }
    public void setScope(Scope scope) { this.scope = scope; }
    public void setName(Formula name) { this.name = name; }
    public void setText(Formula text) { this.text = text; }
    public void setAlignment(int alignment) { this.alignment = alignment; }
    public void setAndroidStringProvider(AndroidStringProvider androidStringProvider) { this.androidStringProvider = androidStringProvider; }
}
