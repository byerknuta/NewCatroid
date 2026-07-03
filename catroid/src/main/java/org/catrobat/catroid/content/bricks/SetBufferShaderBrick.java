package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetBufferShaderBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public SetBufferShaderBrick() {
        super();
        addAllowedBrickField(BrickField.IF_CONDITION, R.id.brick_buffer_shader_name);
        addAllowedBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, R.id.brick_buffer_shader_vsh);
        addAllowedBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, R.id.brick_buffer_shader_fsh);
    }

    public SetBufferShaderBrick(String bufferName, String vsh, String fsh) {
        this();
        setFormulaWithBrickField(BrickField.IF_CONDITION, new Formula(bufferName));
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, new Formula(vsh));
        setFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, new Formula(fsh));
    }

    public SetBufferShaderBrick(Formula bufferName, Formula vsh, Formula fsh) {
        this();
        setFormulaWithBrickField(BrickField.IF_CONDITION, bufferName);
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, vsh);
        setFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, fsh);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_buffer_shader;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createSetBufferShaderAction(
                sprite,
                sequence,
                getFormulaWithBrickField(BrickField.IF_CONDITION),
                getFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE),
                getFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE)
        ));
    }
}
