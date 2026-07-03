package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class StopThreadBrick extends FormulaBrick {

    private static final long serialVersionUID = 1L;

    public StopThreadBrick() {
        super();
        addAllowedBrickField(BrickField.IF_CONDITION, R.id.brick_stop_thread_id_edit);
    }

    public StopThreadBrick(Formula threadId) {
        this();
        setFormulaWithBrickField(BrickField.IF_CONDITION, threadId);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_stop_thread;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createStopThreadAction(
                sprite,
                sequence,
                getFormulaWithBrickField(BrickField.IF_CONDITION)
        ));
    }
}
