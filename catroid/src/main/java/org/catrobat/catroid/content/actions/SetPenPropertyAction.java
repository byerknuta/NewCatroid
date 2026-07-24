package org.catrobat.catroid.content.actions;

import android.util.Log;
import com.badlogic.gdx.scenes.scene2d.Action;
import org.catrobat.catroid.content.Scope;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.stage.PenActor;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.stage.StageListener;

public class SetPenPropertyAction extends Action {
    private Scope scope;
    private int propertySelection;
    private Formula valueFormula;

    @Override
    public boolean act(float delta) {
        try {
            StageListener listener = StageActivity.getActiveStageListener();
            if (listener == null || valueFormula == null) return true;

            PenActor penActor = listener.getPenActor();
            if (penActor == null) return true;

            switch (propertySelection) {
                case 0: // Layer
                    int layer = valueFormula.interpretInteger(scope);
                    listener.setActorZIndexSafely(penActor, layer);
                    break;
                case 1: // Transparency
                    float transVal = valueFormula.interpretFloat(scope);
                    float alpha = 1f - (transVal / 100f);
                    penActor.setPenAlpha(alpha);
                    break;
                case 2: // Blend Mode
                    int mode = valueFormula.interpretInteger(scope);
                    penActor.setBlendMode(mode);
                    break;
                case 3: // Auto-Update
                    int auto = valueFormula.interpretInteger(scope);
                    penActor.setAutoRedraw(auto != 0);
                    break;
            }
        } catch (Exception e) {
            Log.e("SetPenPropertyAction", "Error setting pen property", e);
        }
        return true;
    }

    public void setScope(Scope scope) { this.scope = scope; }
    public void setPropertySelection(int propertySelection) { this.propertySelection = propertySelection; }
    public void setValueFormula(Formula valueFormula) { this.valueFormula = valueFormula; }
}
