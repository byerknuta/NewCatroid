package org.catrobat.catroid.content.actions;

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import org.catrobat.catroid.content.Scope;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.stage.StageActivity;

public class ThreedCreateFixedConstraintAction extends TemporalAction {
    public Scope scope;
    public Formula constraintId, objA, objB;

    public Formula ax, ay, az;
    public Formula bx, by, bz;

    public int mode; // 0 = Auto, 1 = Manual

    @Override
    protected void update(float percent) {
        var tm = StageActivity.getActiveStageListener().getThreeDManager();
        if (tm == null) return;

        try {
            String cId = constraintId.interpretString(scope);
            String idA = objA.interpretString(scope);
            String idB = objB.interpretString(scope);

            if (cId.isEmpty() || idA.isEmpty() || idB.isEmpty()) return;

            if (mode == 0) {
                tm.createFixedConstraintWeld(cId, idA, idB);
            } else {
                float f_ax = ax.interpretFloat(scope);
                float f_ay = ay.interpretFloat(scope);
                float f_az = az.interpretFloat(scope);
                float f_bx = bx.interpretFloat(scope);
                float f_by = by.interpretFloat(scope);
                float f_bz = bz.interpretFloat(scope);

                tm.createFixedConstraintManual(cId, idA, idB, f_ax, f_ay, f_az, f_bx, f_by, f_bz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}