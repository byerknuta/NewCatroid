package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PenDrawTriangleBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PenDrawTriangleBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pen_tri_x1);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_pen_tri_y1);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_pen_tri_x2);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_pen_tri_y2);
        addAllowedBrickField(BrickField.VALUE_5, R.id.brick_pen_tri_x3);
        addAllowedBrickField(BrickField.VALUE_6, R.id.brick_pen_tri_y3);
        addAllowedBrickField(BrickField.VALUE_7, R.id.brick_pen_tri_fill);
    }

    public PenDrawTriangleBrick(double x1, double y1, double x2, double y2, double x3, double y3, int fill) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(x1));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(y1));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(x2));
        setFormulaWithBrickField(BrickField.VALUE_4, new Formula(y2));
        setFormulaWithBrickField(BrickField.VALUE_5, new Formula(x3));
        setFormulaWithBrickField(BrickField.VALUE_6, new Formula(y3));
        setFormulaWithBrickField(BrickField.VALUE_7, new Formula(fill));
    }

    @Override
    public int getViewResource() { return R.layout.brick_pen_draw_triangle; }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createPenDrawTriangleAction(sprite, sequence,
                getFormulaWithBrickField(BrickField.VALUE_1),
                getFormulaWithBrickField(BrickField.VALUE_2),
                getFormulaWithBrickField(BrickField.VALUE_3),
                getFormulaWithBrickField(BrickField.VALUE_4),
                getFormulaWithBrickField(BrickField.VALUE_5),
                getFormulaWithBrickField(BrickField.VALUE_6),
                getFormulaWithBrickField(BrickField.VALUE_7)));
    }
}
