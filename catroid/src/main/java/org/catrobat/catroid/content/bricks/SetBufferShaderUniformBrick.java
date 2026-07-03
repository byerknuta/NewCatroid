package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetBufferShaderUniformBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;
    private int paramCount = 1;

    public SetBufferShaderUniformBrick() {
        super();
        addAllowedBrickField(BrickField.IF_CONDITION, R.id.brick_buf_uniform_buf_name);
        addAllowedBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, R.id.brick_buf_uniform_name);
        addAllowedBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, R.id.brick_buf_uniform_v1);
        addAllowedBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_INDEX, R.id.brick_buf_uniform_v2);
        addAllowedBrickField(BrickField.TIMES_TO_REPEAT, R.id.brick_buf_uniform_v3);
    }

    public SetBufferShaderUniformBrick(String bufferName, String uniformName, float v1, float v2, float v3, int paramCount) {
        this();
        this.paramCount = paramCount;
        setFormulaWithBrickField(BrickField.IF_CONDITION, new Formula(bufferName));
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, new Formula(uniformName));
        setFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, new Formula(v1));
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_INDEX, new Formula(v2));
        setFormulaWithBrickField(BrickField.TIMES_TO_REPEAT, new Formula(v3));
    }

    public SetBufferShaderUniformBrick(Formula bufferName, Formula uniformName, Formula v1, Formula v2, Formula v3, int paramCount) {
        this();
        this.paramCount = paramCount;
        setFormulaWithBrickField(BrickField.IF_CONDITION, bufferName);
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, uniformName);
        setFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, v1);
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_INDEX, v2);
        setFormulaWithBrickField(BrickField.TIMES_TO_REPEAT, v3);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_buffer_uniform;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createSetBufferShaderUniformAction(
                sprite,
                sequence,
                getFormulaWithBrickField(BrickField.IF_CONDITION),
                getFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE),
                getFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE),
                getFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_INDEX),
                getFormulaWithBrickField(BrickField.TIMES_TO_REPEAT),
                paramCount
        ));
    }
}
