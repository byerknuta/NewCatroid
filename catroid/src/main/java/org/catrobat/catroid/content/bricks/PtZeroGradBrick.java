package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;

public class PtZeroGradBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PtZeroGradBrick() {
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_zero_grad;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createPtZeroGradAction(sprite, sequence));
    }
}
