package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;

public class PenFlushBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PenFlushBrick() {}

    @Override
    public int getViewResource() {
        return R.layout.brick_pen_flush;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createPenFlushAction(sprite, sequence));
    }
}
