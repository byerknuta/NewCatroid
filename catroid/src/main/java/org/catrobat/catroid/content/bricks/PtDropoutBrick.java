package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtDropoutBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PtDropoutBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_pt_drop_res);
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pt_drop_input);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_pt_drop_prob);
    }

    public PtDropoutBrick(String res, String input, float prob) {
        this();
        setFormulaWithBrickField(BrickField.NAME, new Formula(res));
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(input));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(prob));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_dropout;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createPtDropoutAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.NAME),
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2)));
    }
}
