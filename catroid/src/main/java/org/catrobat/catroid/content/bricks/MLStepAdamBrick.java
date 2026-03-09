package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class MLStepAdamBrick extends FormulaBrick {
    public MLStepAdamBrick() {
        addAllowedBrickField(BrickField.VALUE, R.id.brick_ml_edit_lr);
    }
    public MLStepAdamBrick(Formula lr) {
        this();
        setFormulaWithBrickField(BrickField.VALUE, lr);
    }
    @Override public int getViewResource() { return R.layout.brick_ml_step_adam; }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createMLStepAdamAction(sprite, sequence, getFormulaWithBrickField(BrickField.VALUE)));
    }
}