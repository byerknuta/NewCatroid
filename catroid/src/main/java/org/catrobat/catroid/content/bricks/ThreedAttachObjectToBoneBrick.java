package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class ThreedAttachObjectToBoneBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public ThreedAttachObjectToBoneBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_attach_child_id);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_attach_parent_id);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_attach_bone_name);
        addAllowedBrickField(BrickField.X, R.id.brick_attach_ox);
        addAllowedBrickField(BrickField.Y, R.id.brick_attach_oy);
        addAllowedBrickField(BrickField.Z, R.id.brick_attach_oz);
    }

    public ThreedAttachObjectToBoneBrick(String childId, String parentId, String boneName, double ox, double oy, double oz) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(childId));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(parentId));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(boneName));
        setFormulaWithBrickField(BrickField.X, new Formula(ox));
        setFormulaWithBrickField(BrickField.Y, new Formula(oy));
        setFormulaWithBrickField(BrickField.Z, new Formula(oz));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_threed_attach_object_to_bone;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createThreedAttachObjectToBoneAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.X),
                        getFormulaWithBrickField(BrickField.Y),
                        getFormulaWithBrickField(BrickField.Z)));
    }
}