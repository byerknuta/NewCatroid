package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetMainRenderLoopsBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public SetMainRenderLoopsBrick() {
        super();
        addAllowedBrickField(BrickField.IF_CONDITION, R.id.brick_render_loop_2d);
        addAllowedBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, R.id.brick_render_loop_fast2d);
        addAllowedBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, R.id.brick_render_loop_3d);
    }

    public SetMainRenderLoopsBrick(float r2d, float rfast2d, float r3d) {
        this();
        setFormulaWithBrickField(BrickField.IF_CONDITION, new Formula(r2d));
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, new Formula(rfast2d));
        setFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, new Formula(r3d));
    }

    public SetMainRenderLoopsBrick(Formula r2d, Formula rfast2d, Formula r3d) {
        this();
        setFormulaWithBrickField(BrickField.IF_CONDITION, r2d);
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, rfast2d);
        setFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, r3d);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_main_render_loops;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createSetMainRenderLoopsAction(
                sprite,
                sequence,
                getFormulaWithBrickField(BrickField.IF_CONDITION),
                getFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE),
                getFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE)
        ));
    }
}
