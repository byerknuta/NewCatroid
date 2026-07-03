package org.catrobat.catroid.content.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.formulaeditor.UserVariable;
import org.catrobat.catroid.stage.ShowTextActor;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.utils.ShowTextUtils.AndroidStringProvider;

public class HideTextAction extends Action {
    private UserVariable variableToHide;
    private Sprite sprite;
    private AndroidStringProvider androidStringProvider;

    @Override
    public boolean act(float delta) {
        if (StageActivity.getActiveStageListener() != null && variableToHide != null) {
            Array<Actor> stageActors = StageActivity.getActiveStageListener().getStage().getActors();
            for (Actor actor : stageActors) {
                if (actor instanceof ShowTextActor) {
                    ShowTextActor sta = (ShowTextActor) actor;
                    if (sta.getVariableNameToCompare().equals(variableToHide.getName()) && sta.getSprite().equals(sprite)) {
                        sta.remove();
                        break;
                    }
                }
            }
        }
        if (variableToHide != null) variableToHide.setVisible(false);
        return true;
    }

    public void setVariableToHide(UserVariable userVariable) { this.variableToHide = userVariable; }
    public void setSprite(Sprite sprite) { this.sprite = sprite; }
    public void setAndroidStringProvider(AndroidStringProvider androidStringProvider) { this.androidStringProvider = androidStringProvider; }
}
