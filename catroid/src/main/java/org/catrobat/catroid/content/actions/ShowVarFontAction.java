package org.catrobat.catroid.content.actions;

import android.graphics.Typeface;
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ShowVarFontAction extends Action {
    private Formula xPosition, yPosition, relativeTextSize, color, file;
    private UserVariable variableToShow;
    private Scope scope;
    private int alignment;
    private AndroidStringProvider androidStringProvider;

    private static final Map<String, Typeface> fontCache = new HashMap<>();

    @Override
    public boolean act(float delta) {
        try {
            float xPos = xPosition.interpretFloat(scope);
            float yPos = yPosition.interpretFloat(scope);
            float relSize = relativeTextSize.interpretFloat(scope) / 100f;
            String colStr = color.interpretString(scope);
            String fileFont = file.interpretString(scope) != null ? file.interpretString(scope) : "font.ttf";

            File f = scope.getProject().getFile(fileFont);
            if (f == null || !f.exists()) return true;

            String fontPath = f.getAbsolutePath();
            if (!fontCache.containsKey(fontPath)) fontCache.put(fontPath, Typeface.createFromFile(f));
            Typeface typeface = fontCache.get(fontPath);

            if (StageActivity.getActiveStageListener() == null) return true;

            Array<Actor> stageActors = StageActivity.getActiveStageListener().getStage().getActors();
            ShowTextActor targetActor = null;

            for (Actor a : stageActors) {
                if (a instanceof ShowTextActor) {
                    ShowTextActor sta = (ShowTextActor) a;
                    if (sta.getVariableNameToCompare().equals(variableToShow.getName()) && sta.getSprite().equals(scope.getSprite())) {
                        targetActor = sta;
                        break;
                    }
                }
            }

            if (targetActor == null) {
                targetActor = new ShowTextActor(false, variableToShow, "", xPos, yPos, relSize, colStr, scope.getSprite(), alignment, androidStringProvider);
                targetActor.setFont(typeface);
                StageActivity.getActiveStageListener().addActor(targetActor);
                variableToShow.setVisible(true);
            } else {
                targetActor.updateProperties(targetActor.getVariableNameToCompare(), xPos, yPos, relSize, colStr, typeface);
            }
        } catch (InterpretationException e) {
            Log.d("ShowVarFontAction", "InterpretationException: " + e);
        }
        return true;
    }

    public void setPosition(Formula xPosition, Formula yPosition) { this.xPosition = xPosition; this.yPosition = yPosition; }
    public void setRelativeTextSize(Formula relativeTextSize) { this.relativeTextSize = relativeTextSize; }
    public void setColor(Formula color) { this.color = color; }
    public void setScope(Scope scope) { this.scope = scope; }
    public void setFile(Formula file) { this.file = file; }
    public void setVariableToShow(UserVariable userVariable) { this.variableToShow = userVariable; }
    public void setAlignment(int alignment) { this.alignment = alignment; }
    public void setAndroidStringProvider(AndroidStringProvider androidStringProvider) { this.androidStringProvider = androidStringProvider; }
}
