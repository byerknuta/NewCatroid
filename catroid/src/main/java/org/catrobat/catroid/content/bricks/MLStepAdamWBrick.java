package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class MLStepAdamWBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public MLStepAdamWBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_ml_adamw_lr);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_ml_adamw_decay);
    }

    public MLStepAdamWBrick(float lr, float decay) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(lr));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(decay));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_ml_step_adamw;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createMLStepAdamWAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2)));
    }
}
