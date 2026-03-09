package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtCreateTensorBrick extends FormulaBrick {
    public PtCreateTensorBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_pt_create_name);
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pt_create_shape);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_pt_create_value);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_pt_create_trainable);
    }

    public PtCreateTensorBrick(String name, String shape, float val, boolean train) {
        this();
        setFormulaWithBrickField(BrickField.NAME, new Formula(name));
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(shape));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(val));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(train ? 1 : 0));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_create_tensor;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createPtCreateTensorAction( sprite, sequence,
                getFormulaWithBrickField(BrickField.NAME),
                getFormulaWithBrickField(BrickField.VALUE_1),
                getFormulaWithBrickField(BrickField.VALUE_2),
                getFormulaWithBrickField(BrickField.VALUE_3)
        ));
    }
}