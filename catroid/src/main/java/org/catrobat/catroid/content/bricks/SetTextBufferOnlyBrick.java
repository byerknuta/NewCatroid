package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetTextBufferOnlyBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public SetTextBufferOnlyBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_set_text_buffer_only_name);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_set_text_buffer_only_state);
    }

    public SetTextBufferOnlyBrick(String textName, int state) {
        this(new Formula(textName), new Formula(state));
    }

    public SetTextBufferOnlyBrick(Formula textName, Formula state) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, textName);
        setFormulaWithBrickField(BrickField.VALUE_2, state);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_text_buffer_only;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createSetTextBufferOnlyAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2)));
    }
}
