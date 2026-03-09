package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtReshapeBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PtReshapeBrick() {
        addAllowedBrickField(Brick.BrickField.NAME, R.id.brick_pt_reshape_name);
        addAllowedBrickField(Brick.BrickField.VALUE_1, R.id.brick_pt_reshape_shape);
    }

    public PtReshapeBrick(String tensorName, String newShape) {
        this();
        setFormulaWithBrickField(BrickField.NAME, new Formula(tensorName));
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(newShape));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_reshape;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createPtReshapeAction( sprite, sequence,
                getFormulaWithBrickField(Brick.BrickField.NAME),
                getFormulaWithBrickField(Brick.BrickField.VALUE_1)
        ));
    }
}