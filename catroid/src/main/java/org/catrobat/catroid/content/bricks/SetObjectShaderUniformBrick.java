package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetObjectShaderUniformBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public SetObjectShaderUniformBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_object_shader_uniform_id);
        addAllowedBrickField(BrickField.TEXT, R.id.brick_object_shader_uniform_name);
        addAllowedBrickField(BrickField.X, R.id.brick_object_shader_uniform_val1);
        addAllowedBrickField(BrickField.Y, R.id.brick_object_shader_uniform_val2);
        addAllowedBrickField(BrickField.Z, R.id.brick_object_shader_uniform_val3);
    }

    public SetObjectShaderUniformBrick(String objectId, String name, float v1, float v2, float v3) {
        this(new Formula(objectId), new Formula(name), new Formula(v1), new Formula(v2), new Formula(v3));
    }

    public SetObjectShaderUniformBrick(Formula objectId, Formula name, Formula v1, Formula v2, Formula v3) {
        this();
        setFormulaWithBrickField(BrickField.NAME, objectId);
        setFormulaWithBrickField(BrickField.TEXT, name);
        setFormulaWithBrickField(BrickField.X, v1);
        setFormulaWithBrickField(BrickField.Y, v2);
        setFormulaWithBrickField(BrickField.Z, v3);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_object_shader_uniform;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createSetObjectShaderUniformAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.NAME),
                        getFormulaWithBrickField(BrickField.TEXT),
                        getFormulaWithBrickField(BrickField.X),
                        getFormulaWithBrickField(BrickField.Y),
                        getFormulaWithBrickField(BrickField.Z)));
    }
}
