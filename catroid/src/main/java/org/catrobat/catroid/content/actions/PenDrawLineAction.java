package org.catrobat.catroid.content.actions;

import android.util.Log;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;

import org.catrobat.catroid.content.Scope;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.stage.PenActor;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.stage.StageListener;

public class PenDrawLineAction extends Action {
    private Scope scope;
    private Formula x1, y1, x2, y2, thickness;

    @Override
    public boolean act(float delta) {
        try {
            StageListener listener = StageActivity.getActiveStageListener();
            if (listener == null) return true;

            PenActor penActor = listener.getPenActor();
            if (penActor == null) return true;

            float lineX1 = x1 != null ? x1.interpretFloat(scope) : 0f;
            float lineY1 = y1 != null ? y1.interpretFloat(scope) : 0f;
            float lineX2 = x2 != null ? x2.interpretFloat(scope) : 0f;
            float lineY2 = y2 != null ? y2.interpretFloat(scope) : 0f;
            float lineThick = thickness != null ? thickness.interpretFloat(scope) : 5f;

            Color color = new Color();
            if (scope.getSprite() != null && scope.getSprite().penConfiguration != null) {
                var pc = scope.getSprite().penConfiguration.getPenColor();
                color.set(pc.r, pc.g, pc.b, pc.a);
            } else {
                color.set(0, 0, 1, 1);
            }

            penActor.drawDirectLine(lineX1, lineY1, lineX2, lineY2, lineThick, color);

        } catch (Exception e) {
            Log.e("PenDrawLineAction", "Error drawing line", e);
        }
        return true;
    }

    public void setScope(Scope scope) { this.scope = scope; }
    public void setFormulas(Formula x1, Formula y1, Formula x2, Formula y2, Formula thickness) {
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.thickness = thickness;
    }
}
