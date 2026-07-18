package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class Replace3DModelBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public Replace3DModelBrick() {
        addAllowedBrickField(Brick.BrickField.NAME, R.id.brick_replace_3d_model_edit_object_id);
        addAllowedBrickField(Brick.BrickField.TEXT, R.id.brick_replace_3d_model_edit_model_path);
    }

    public Replace3DModelBrick(String objectId, String modelPath) {
        this(new Formula(objectId), new Formula(modelPath));
    }

    public Replace3DModelBrick(Formula objectId, Formula modelPath) {
        this();
        setFormulaWithBrickField(BrickField.NAME, objectId);
        setFormulaWithBrickField(BrickField.TEXT, modelPath);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_replace_3d_model;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createReplace3DModelAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.NAME), getFormulaWithBrickField(BrickField.TEXT)));
    }
}
