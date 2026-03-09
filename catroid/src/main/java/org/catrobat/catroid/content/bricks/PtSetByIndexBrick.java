package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtSetByIndexBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PtSetByIndexBrick() {
        addAllowedBrickField(Brick.BrickField.NAME, R.id.brick_pt_set_index_name);
        addAllowedBrickField(Brick.BrickField.VALUE_1, R.id.brick_pt_set_index_idx);
        addAllowedBrickField(Brick.BrickField.VALUE_2, R.id.brick_pt_set_index_val);
    }

    public PtSetByIndexBrick(String name, int index, float value) {
        this();
        setFormulaWithBrickField(BrickField.NAME, new Formula(name));
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(index));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(value));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_set_by_index;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createPtSetByIndexAction( sprite, sequence,
                getFormulaWithBrickField(Brick.BrickField.NAME),
                getFormulaWithBrickField(Brick.BrickField.VALUE_1),
                getFormulaWithBrickField(Brick.BrickField.VALUE_2)
        ));
    }
}