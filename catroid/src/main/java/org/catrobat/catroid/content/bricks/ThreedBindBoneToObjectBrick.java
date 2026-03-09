package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class ThreedBindBoneToObjectBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public ThreedBindBoneToObjectBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_bind_bone_name);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_bind_model_id);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_bind_target_id);
    }

    public ThreedBindBoneToObjectBrick(String boneName, String modelId, String targetId) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(boneName));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(modelId));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(targetId));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_threed_bind_bone_to_object;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createThreedBindBoneToObjectAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3)));
    }
}