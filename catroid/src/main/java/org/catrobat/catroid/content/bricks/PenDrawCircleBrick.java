package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PenDrawCircleBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PenDrawCircleBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pen_circle_x);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_pen_circle_y);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_pen_circle_radius);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_pen_circle_start_angle);
        addAllowedBrickField(BrickField.VALUE_5, R.id.brick_pen_circle_sweep_angle);
        addAllowedBrickField(BrickField.VALUE_6, R.id.brick_pen_circle_fill);
    }

    public PenDrawCircleBrick(double x, double y, double radius, double startAngle, double sweepDegrees, int fill) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(x));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(y));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(radius));
        setFormulaWithBrickField(BrickField.VALUE_4, new Formula(startAngle));
        setFormulaWithBrickField(BrickField.VALUE_5, new Formula(sweepDegrees));
        setFormulaWithBrickField(BrickField.VALUE_6, new Formula(fill));
    }

    @Override
    public int getViewResource() { return R.layout.brick_pen_draw_circle; }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createPenDrawCircleAction(sprite, sequence,
                getFormulaWithBrickField(BrickField.VALUE_1),
                getFormulaWithBrickField(BrickField.VALUE_2),
                getFormulaWithBrickField(BrickField.VALUE_3),
                getFormulaWithBrickField(BrickField.VALUE_4),
                getFormulaWithBrickField(BrickField.VALUE_5),
                getFormulaWithBrickField(BrickField.VALUE_6)));
    }
}
