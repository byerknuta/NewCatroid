package org.catrobat.catroid.content.actions;

import android.util.Log;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;

import org.catrobat.catroid.content.Scope;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.stage.PenActor;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.stage.StageListener;

public class PenDrawCircleAction extends Action {
    private Scope scope;
    private Formula x, y, radius, startAngle, degrees, fill;

    @Override
    public boolean act(float delta) {
        try {
            StageListener listener = StageActivity.getActiveStageListener();
            if (listener == null) return true;

            PenActor penActor = listener.getPenActor();
            if (penActor == null) return true;

            float cX = x != null ? x.interpretFloat(scope) : 0f;
            float cY = y != null ? y.interpretFloat(scope) : 0f;
            float cRad = radius != null ? Math.abs(radius.interpretFloat(scope)) : 50f;
            float cStartAngle = startAngle != null ? startAngle.interpretFloat(scope) : 0f;
            float cDegrees = degrees != null ? degrees.interpretFloat(scope) : 360f;
            boolean isFill = fill != null && fill.interpretInteger(scope) != 0;

            Color color = new Color();
            if (scope.getSprite() != null && scope.getSprite().penConfiguration != null) {
                var pc = scope.getSprite().penConfiguration.getPenColor();
                color.set(pc.r, pc.g, pc.b, pc.a);
            } else {
                color.set(0, 0, 1, 1);
            }

            penActor.drawDirectCircleOrArc(cX, cY, cRad, cStartAngle, cDegrees, isFill, color);

        } catch (Exception e) {
            Log.e("PenDrawCircleAction", "Error drawing circle/arc", e);
        }
        return true;
    }

    public void setScope(Scope scope) { this.scope = scope; }
    public void setFormulas(Formula x, Formula y, Formula radius, Formula startAngle, Formula degrees, Formula fill) {
        this.x = x; this.y = y; this.radius = radius;
        this.startAngle = startAngle; this.degrees = degrees; this.fill = fill;
    }
}
