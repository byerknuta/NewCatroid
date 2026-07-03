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

public class ShowTextRotationAction extends Action {
    private Formula xPosition, yPosition, relativeTextSize, color, text, file, name, rotation;
    private Scope scope;
    private int alignment;
    private AndroidStringProvider androidStringProvider;

    private static final Map<String, Typeface> fontCache = new HashMap<>();

    @Override
    public boolean act(float delta) {
        try {
            String nameStr = name.interpretString(scope) != null ? name.interpretString(scope) : "dummyActor";
            String textStr = text.interpretString(scope) != null ? text.interpretString(scope) : "NaN";
            String fileFont = file.interpretString(scope) != null ? file.interpretString(scope) : "font.ttf";
            float rotationFl = rotation.interpretFloat(scope);
            float xPos = xPosition.interpretFloat(scope);
            float yPos = yPosition.interpretFloat(scope);
            float relSize = relativeTextSize.interpretFloat(scope) / 100f;
            String colStr = color.interpretString(scope);

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
                    if (sta.getVariableNameToCompare().equals(nameStr) && sta.getSprite().equals(scope.getSprite())) {
                        targetActor = sta;
                        break;
                    }
                }
            }

            if (targetActor == null) {
                UserVariable variableToShow = new UserVariable(nameStr, textStr);
                targetActor = new ShowTextActor(true, variableToShow, textStr, xPos, yPos, relSize, colStr, scope.getSprite(), alignment, androidStringProvider);
                targetActor.setFont(typeface);
                targetActor.setRotation(rotationFl);
                StageActivity.getActiveStageListener().addActor(targetActor);
                variableToShow.setVisible(true);
            } else {
                targetActor.updateProperties(textStr, xPos, yPos, relSize, colStr, typeface);
                targetActor.setRotation(rotationFl);
            }
        } catch (InterpretationException e) {
            Log.d("ShowTextRotation", "InterpretationException: " + e);
        }
        return true;
    }

    public void setPosition(Formula xPosition, Formula yPosition) { this.xPosition = xPosition; this.yPosition = yPosition; }
    public void setRelativeTextSize(Formula relativeTextSize) { this.relativeTextSize = relativeTextSize; }
    public void setColor(Formula color) { this.color = color; }
    public void setScope(Scope scope) { this.scope = scope; }
    public void setText(Formula text) { this.text = text; }
    public void setRotation(Formula rotation) { this.rotation = rotation; }
    public void setFile(Formula file) { this.file = file; }
    public void setName(Formula name) { this.name = name; }
    public void setAlignment(int alignment) { this.alignment = alignment; }
    public void setAndroidStringProvider(AndroidStringProvider androidStringProvider) { this.androidStringProvider = androidStringProvider; }
}
