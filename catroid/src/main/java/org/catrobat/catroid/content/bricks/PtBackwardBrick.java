package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtBackwardBrick extends FormulaBrick {

    public PtBackwardBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_pt_backward_loss);
    }

    public PtBackwardBrick(String lossName) {
        this();
        setFormulaWithBrickField(BrickField.NAME, new Formula(lossName));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_backward;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createPtBackwardAction( sprite, sequence,
                getFormulaWithBrickField(BrickField.NAME)
        ));
    }
}