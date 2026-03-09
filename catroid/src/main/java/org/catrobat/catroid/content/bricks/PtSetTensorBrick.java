package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtSetTensorBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PtSetTensorBrick() {
        addAllowedBrickField(Brick.BrickField.NAME, R.id.brick_pt_set_tensor_name);
        addAllowedBrickField(Brick.BrickField.VALUE_1, R.id.brick_pt_set_tensor_data);
    }

    public PtSetTensorBrick(String name, String data) {
        this();
        setFormulaWithBrickField(BrickField.NAME, new Formula(name));
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(data));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_set_tensor;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createPtSetTensorAction( sprite, sequence,
                getFormulaWithBrickField(Brick.BrickField.NAME),
                getFormulaWithBrickField(Brick.BrickField.VALUE_1)
        ));
    }
}