package org.catrobat.catroid.content.actions;

import android.util.Log;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;

import org.catrobat.catroid.content.Scope;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.stage.PenActor;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.stage.StageListener;

public class PenDrawRectAction extends Action {
    private Scope scope;
    private Formula x, y, width, height, fill;

    @Override
    public boolean act(float delta) {
        try {
            StageListener listener = StageActivity.getActiveStageListener();
            if (listener == null) return true;

            PenActor penActor = listener.getPenActor();
            if (penActor == null) return true;

            float rX = x != null ? x.interpretFloat(scope) : 0f;
            float rY = y != null ? y.interpretFloat(scope) : 0f;
            float rW = width != null ? width.interpretFloat(scope) : 100f;
            float rH = height != null ? height.interpretFloat(scope) : 100f;
            boolean isFill = fill != null && fill.interpretInteger(scope) != 0;

            Color color = new Color();
            if (scope.getSprite() != null && scope.getSprite().penConfiguration != null) {
                var pc = scope.getSprite().penConfiguration.getPenColor();
                color.set(pc.r, pc.g, pc.b, pc.a);
            } else {
                color.set(0, 0, 1, 1);
            }

            penActor.drawDirectRect(rX, rY, rW, rH, isFill, color);

        } catch (Exception e) {
            Log.e("PenDrawRectAction", "Error drawing rectangle", e);
        }
        return true;
    }

    public void setScope(Scope scope) { this.scope = scope; }
    public void setFormulas(Formula x, Formula y, Formula width, Formula height, Formula fill) {
        this.x = x; this.y = y; this.width = width; this.height = height; this.fill = fill;
    }
}
