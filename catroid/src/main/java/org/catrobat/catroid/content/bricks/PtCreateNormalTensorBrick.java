package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtCreateNormalTensorBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PtCreateNormalTensorBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_pt_normal_name);
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pt_normal_shape);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_pt_normal_mean);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_pt_normal_std);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_pt_normal_trainable);
    }

    public PtCreateNormalTensorBrick(String name, String shape, float mean, float std, boolean trainable) {
        this();
        setFormulaWithBrickField(BrickField.NAME, new Formula(name));
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(shape));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(mean));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(std));
        setFormulaWithBrickField(BrickField.VALUE_4, new Formula(trainable ? 1 : 0));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_create_normal_tensor;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createPtCreateNormalTensorAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.NAME),
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.VALUE_4)));
    }
}
