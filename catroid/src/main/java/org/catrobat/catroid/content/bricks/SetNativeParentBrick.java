package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetNativeParentBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public SetNativeParentBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_set_native_parent_id_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_set_native_parent_target_field);
    }

    public SetNativeParentBrick(String viewId, String parentId) {
        this(new Formula(viewId), new Formula(parentId));
    }

    public SetNativeParentBrick(Formula viewId, Formula parentId) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, viewId);
        setFormulaWithBrickField(BrickField.VALUE_2, parentId);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_native_parent;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createSetNativeParentAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2)));
    }
}
