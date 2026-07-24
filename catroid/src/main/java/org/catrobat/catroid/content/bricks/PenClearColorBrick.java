package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PenClearColorBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PenClearColorBrick() {
        addAllowedBrickField(BrickField.COLOR, R.id.brick_pen_clear_color_val);
        addAllowedBrickField(BrickField.VALUE, R.id.brick_pen_clear_alpha_val);
    }

    public PenClearColorBrick(String colorHex, double transparency) {
        this();
        setFormulaWithBrickField(BrickField.COLOR, new Formula(colorHex));
        setFormulaWithBrickField(BrickField.VALUE, new Formula(transparency));
    }

    @Override
    public int getViewResource() { return R.layout.brick_pen_clear_color; }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createPenClearColorAction(sprite, sequence,
                getFormulaWithBrickField(BrickField.COLOR),
                getFormulaWithBrickField(BrickField.VALUE)));
    }
}
