package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PenDrawRectBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PenDrawRectBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pen_rect_x);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_pen_rect_y);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_pen_rect_w);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_pen_rect_h);
        addAllowedBrickField(BrickField.VALUE_5, R.id.brick_pen_rect_fill);
    }

    public PenDrawRectBrick(double x, double y, double width, double height, int fill) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(x));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(y));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(width));
        setFormulaWithBrickField(BrickField.VALUE_4, new Formula(height));
        setFormulaWithBrickField(BrickField.VALUE_5, new Formula(fill));
    }

    @Override
    public int getViewResource() { return R.layout.brick_pen_draw_rect; }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createPenDrawRectAction(sprite, sequence,
                getFormulaWithBrickField(BrickField.VALUE_1),
                getFormulaWithBrickField(BrickField.VALUE_2),
                getFormulaWithBrickField(BrickField.VALUE_3),
                getFormulaWithBrickField(BrickField.VALUE_4),
                getFormulaWithBrickField(BrickField.VALUE_5)));
    }
}
