package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtMaxPool2DBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PtMaxPool2DBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_pt_pool_res);
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pt_pool_input);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_pt_pool_size);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_pt_pool_stride);
    }

    public PtMaxPool2DBrick(String res, String input, int poolSize, int stride) {
        this();
        setFormulaWithBrickField(BrickField.NAME, new Formula(res));
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(input));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(poolSize));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(stride));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_maxpool2d;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createPtMaxPool2DAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.NAME),
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3)));
    }
}
