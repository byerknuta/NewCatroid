package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class BroadcastWithParamsBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public BroadcastWithParamsBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_broadcast_with_params_edit_signal);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_broadcast_with_params_edit_data);
    }

    public BroadcastWithParamsBrick(String signal, String data) {
        this(new Formula(signal), new Formula(data));
    }

    public BroadcastWithParamsBrick(Formula signal, Formula data) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, signal);
        setFormulaWithBrickField(BrickField.VALUE_2, data);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_broadcast_with_params;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createBroadcastWithParamsAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2)));
    }
}
