package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtStepBrick extends FormulaBrick {

    public PtStepBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pt_step_lr);
    }

    public PtStepBrick(float lr) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(lr));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_step;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createPtStepAction( sprite, sequence,
                getFormulaWithBrickField(BrickField.VALUE_1)
        ));
    }
}