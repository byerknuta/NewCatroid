package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PenDrawLineBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PenDrawLineBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pen_line_x1);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_pen_line_y1);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_pen_line_x2);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_pen_line_y2);
        addAllowedBrickField(BrickField.VALUE_5, R.id.brick_pen_line_thickness);
    }

    public PenDrawLineBrick(double x1, double y1, double x2, double y2, double thickness) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(x1));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(y1));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(x2));
        setFormulaWithBrickField(BrickField.VALUE_4, new Formula(y2));
        setFormulaWithBrickField(BrickField.VALUE_5, new Formula(thickness));
    }

    @Override
    public int getViewResource() { return R.layout.brick_pen_draw_line; }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createPenDrawLineAction(sprite, sequence,
                getFormulaWithBrickField(BrickField.VALUE_1),
                getFormulaWithBrickField(BrickField.VALUE_2),
                getFormulaWithBrickField(BrickField.VALUE_3),
                getFormulaWithBrickField(BrickField.VALUE_4),
                getFormulaWithBrickField(BrickField.VALUE_5)));
    }
}
