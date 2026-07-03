package org.catrobat.catroid.content.actions;

import android.util.Log;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

import org.catrobat.catroid.content.Scope;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.InterpretationException;
import org.catrobat.catroid.formulaeditor.UserVariable;
import org.catrobat.catroid.stage.ShowTextActor;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.utils.ShowTextUtils.AndroidStringProvider;

public class HideText3Action extends Action {
    private String name;
    private Sprite sprite;
    private Scope scope;
    private AndroidStringProvider androidStringProvider;

    @Override
    public boolean act(float delta) {
        if (scope == null) return true;

        UserVariable variableToHide = new UserVariable(name);
        if (StageActivity.getActiveStageListener() != null) {
            Array<Actor> stageActors = StageActivity.getActiveStageListener().getStage().getActors();
            for (Actor actor : stageActors) {
                if (actor instanceof ShowTextActor) {
                    ShowTextActor sta = (ShowTextActor) actor;
                    if (sta.getVariableNameToCompare().equals(name) && sta.getSprite().equals(sprite)) {
                        sta.remove();
                        break;
                    }
                }
            }
        }
        variableToHide.setVisible(false);
        return true;
    }

    public void setName(Formula nameFormula) {
        try {
            this.name = nameFormula.interpretString(this.scope);
        } catch (InterpretationException e) {
            this.name = "defaultName";
        }
    }
    public void setScope(Scope scope) { this.scope = scope; }
    public void setSprite(Sprite sprite) { this.sprite = sprite; }
    public void setAndroidStringProvider(AndroidStringProvider androidStringProvider) { this.androidStringProvider = androidStringProvider; }
}
