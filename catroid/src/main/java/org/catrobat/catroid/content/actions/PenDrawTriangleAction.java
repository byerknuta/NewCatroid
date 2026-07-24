package org.catrobat.catroid.content.actions;

import android.util.Log;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;

import org.catrobat.catroid.content.Scope;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.stage.PenActor;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.stage.StageListener;

public class PenDrawTriangleAction extends Action {
    private Scope scope;
    private Formula x1, y1, x2, y2, x3, y3, fill;

    @Override
    public boolean act(float delta) {
        try {
            StageListener listener = StageActivity.getActiveStageListener();
            if (listener == null) return true;

            PenActor penActor = listener.getPenActor();
            if (penActor == null) return true;

            float tX1 = x1 != null ? x1.interpretFloat(scope) : 0f;
            float tY1 = y1 != null ? y1.interpretFloat(scope) : 0f;
            float tX2 = x2 != null ? x2.interpretFloat(scope) : 0f;
            float tY2 = y2 != null ? y2.interpretFloat(scope) : 0f;
            float tX3 = x3 != null ? x3.interpretFloat(scope) : 0f;
            float tY3 = y3 != null ? y3.interpretFloat(scope) : 0f;
            boolean isFill = fill != null && fill.interpretInteger(scope) != 0;

            Color color = new Color();
            if (scope.getSprite() != null && scope.getSprite().penConfiguration != null) {
                var pc = scope.getSprite().penConfiguration.getPenColor();
                color.set(pc.r, pc.g, pc.b, pc.a);
            } else {
                color.set(0, 0, 1, 1);
            }

            penActor.drawDirectTriangle(tX1, tY1, tX2, tY2, tX3, tY3, isFill, color);

        } catch (Exception e) {
            Log.e("PenDrawTriangleAction", "Error drawing triangle", e);
        }
        return true;
    }

    public void setScope(Scope scope) { this.scope = scope; }
    public void setFormulas(Formula x1, Formula y1, Formula x2, Formula y2, Formula x3, Formula y3, Formula fill) {
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.x3 = x3; this.y3 = y3; this.fill = fill;
    }
}
