package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtSliceBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PtSliceBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_pt_slice_res);
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pt_slice_input);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_pt_slice_start);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_pt_slice_end);
    }

    public PtSliceBrick(String res, String input, int startCol, int endCol) {
        this();
        setFormulaWithBrickField(BrickField.NAME, new Formula(res));
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(input));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(startCol));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(endCol));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_slice;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createPtSliceAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.NAME),
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3)));
    }
}
