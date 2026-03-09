package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtSetTrainingBrick extends FormulaBrick {

    public PtSetTrainingBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pt_set_training_text);
    }

    public PtSetTrainingBrick(int on) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(on));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_set_training;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createPtSetTrainingAction( sprite, sequence,
                getFormulaWithBrickField(BrickField.VALUE_1)
        ));
    }
}