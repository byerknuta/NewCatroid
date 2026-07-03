package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class AddTextToBufferBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public AddTextToBufferBrick() {
        super();
        addAllowedBrickField(BrickField.IF_CONDITION, R.id.brick_add_text_buffer_name);
        addAllowedBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, R.id.brick_add_text_var_name);
    }

    public AddTextToBufferBrick(String varName, String bufferName) {
        this();
        setFormulaWithBrickField(BrickField.IF_CONDITION, new Formula(bufferName));
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, new Formula(varName));
    }

    public AddTextToBufferBrick(Formula varName, Formula bufferName) {
        this();
        setFormulaWithBrickField(BrickField.IF_CONDITION, bufferName);
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, varName);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_add_text_to_buffer;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createAddTextToBufferAction(
                sprite,
                sequence,
                getFormulaWithBrickField(BrickField.IF_CONDITION),
                getFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE)
        ));
    }
}
