package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;

public class DisableBackgroundModeBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public DisableBackgroundModeBrick() {
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_disable_background_mode;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createDisableBackgroundModeAction(sprite, sequence));
    }
}
