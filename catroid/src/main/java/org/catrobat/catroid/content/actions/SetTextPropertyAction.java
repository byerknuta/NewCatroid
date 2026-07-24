package org.catrobat.catroid.content.actions;

import android.util.Log;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

import org.catrobat.catroid.content.Scope;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.stage.ShowTextActor;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.stage.StageListener;

import java.io.File;

public class SetTextPropertyAction extends Action {
    private Scope scope;
    private Formula nameFormula;
    private int propertySelection;
    private Formula valueFormula;

    @Override
    public boolean act(float delta) {
        try {
            StageListener listener = StageActivity.getActiveStageListener();
            if (listener == null || nameFormula == null || valueFormula == null) return true;

            String textName = nameFormula.interpretString(scope);
            if (textName == null || textName.isEmpty()) return true;

            Array<Actor> stageActors = listener.getStage().getActors();
            ShowTextActor targetActor = null;

            for (Actor a : stageActors) {
                if (a instanceof ShowTextActor) {
                    ShowTextActor sta = (ShowTextActor) a;
                    if (textName.equals(sta.getVariableNameToCompare()) && sta.getSprite().equals(scope.getSprite())) {
                        targetActor = sta;
                        break;
                    }
                }
            }

            if (targetActor == null) return true;

            switch (propertySelection) {
                case 0: // Position X
                    targetActor.setPositionX(valueFormula.interpretFloat(scope));
                    break;
                case 1: // Position Y
                    targetActor.setPositionY(valueFormula.interpretFloat(scope));
                    break;
                case 2: // Size
                    targetActor.setRelativeSize(valueFormula.interpretFloat(scope) / 100f);
                    break;
                case 3: // Width
                    targetActor.setScaleX(valueFormula.interpretFloat(scope) / 100f);
                    break;
                case 4: // Height
                    targetActor.setScaleY(valueFormula.interpretFloat(scope) / 100f);
                    break;
                case 5: // Color
                    targetActor.setColorStr(valueFormula.interpretString(scope));
                    break;
                case 6: // Rotation
                    targetActor.setRotationDegrees(valueFormula.interpretFloat(scope));
                    break;
                case 7: // Layer
                    int desiredLayer = valueFormula.interpretInteger(scope);
                    listener.setActorZIndexSafely(targetActor, desiredLayer);
                    break;
                case 8: // Transparency
                    float transPercent = valueFormula.interpretFloat(scope);
                    float alpha = 1f - (transPercent / 100f);
                    targetActor.setAlphaValue(alpha);
                    break;
                case 9: // Alignment (L, C, R)
                    targetActor.setAlignment(valueFormula.interpretInteger(scope));
                    break;
                case 10: // Text
                    targetActor.setRawText(valueFormula.interpretString(scope));
                    break;
                case 11: // Font
                    String fileName = valueFormula.interpretString(scope);
                    if (fileName != null && !fileName.isEmpty() && scope.getProject() != null) {
                        File fontFile = scope.getProject().getFile(fileName);
                        if (fontFile != null && fontFile.exists()) {
                            targetActor.setFontFromFile(fontFile);
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e("SetTextPropertyAction", "Error updating text property", e);
        }
        return true;
    }

    public void setScope(Scope scope) { this.scope = scope; }
    public void setNameFormula(Formula nameFormula) { this.nameFormula = nameFormula; }
    public void setPropertySelection(int propertySelection) { this.propertySelection = propertySelection; }
    public void setValueFormula(Formula valueFormula) { this.valueFormula = valueFormula; }
}
