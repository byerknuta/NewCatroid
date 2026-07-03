package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetBufferEffectsBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public SetBufferEffectsBrick() {
        super();
        addAllowedBrickField(BrickField.IF_CONDITION, R.id.brick_buffer_fx_name);
        addAllowedBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, R.id.brick_buffer_fx_vfx);
        addAllowedBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, R.id.brick_buffer_fx_mip);
    }

    public SetBufferEffectsBrick(String name, float vfx, float mip) {
        this();
        setFormulaWithBrickField(BrickField.IF_CONDITION, new Formula(name));
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, new Formula(vfx));
        setFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, new Formula(mip));
    }

    public SetBufferEffectsBrick(Formula name, Formula vfx, Formula mip) {
        this();
        setFormulaWithBrickField(BrickField.IF_CONDITION, name);
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, vfx);
        setFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, mip);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_buffer_effects;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createSetBufferEffectsAction(
                sprite,
                sequence,
                getFormulaWithBrickField(BrickField.IF_CONDITION),
                getFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE),
                getFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE)
        ));
    }
}
