package org.catrobat.catroid.content.actions;

import android.util.Log;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;

import org.catrobat.catroid.content.Scope;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.stage.PenActor;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.stage.StageListener;

public class PenClearColorAction extends Action {
    private Scope scope;
    private Formula colorFormula, alphaFormula;

    @Override
    public boolean act(float delta) {
        try {
            StageListener listener = StageActivity.getActiveStageListener();
            if (listener == null) return true;

            PenActor penActor = listener.getPenActor();
            if (penActor == null) return true;

            String colStr = colorFormula != null ? colorFormula.interpretString(scope) : "#000000";
            float transPercent = alphaFormula != null ? alphaFormula.interpretFloat(scope) : 0f;
            float alpha = 1f - (transPercent / 100f);

            Color color = Color.valueOf(colStr);
            penActor.clearWithColor(color, alpha);

        } catch (Exception e) {
            Log.e("PenClearColorAction", "Error clearing pen with color", e);
        }
        return true;
    }

    public void setScope(Scope scope) { this.scope = scope; }
    public void setFormulas(Formula colorFormula, Formula alphaFormula) {
        this.colorFormula = colorFormula;
        this.alphaFormula = alphaFormula;
    }
}
