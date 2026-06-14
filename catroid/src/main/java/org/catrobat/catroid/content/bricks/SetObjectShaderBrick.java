package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetObjectShaderBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public SetObjectShaderBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_object_shader_id);
        addAllowedBrickField(BrickField.TEXT, R.id.brick_object_shader_vertex);
        addAllowedBrickField(BrickField.VALUE, R.id.brick_object_shader_fragment);
    }

    public SetObjectShaderBrick(String objectId, String vertex, String fragment) {
        this(new Formula(objectId), new Formula(vertex), new Formula(fragment));
    }

    public SetObjectShaderBrick(Formula objectId, Formula vertex, Formula fragment) {
        this();
        setFormulaWithBrickField(BrickField.NAME, objectId);
        setFormulaWithBrickField(BrickField.TEXT, vertex);
        setFormulaWithBrickField(BrickField.VALUE, fragment);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_object_shader;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createSetObjectShaderAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.NAME),
                        getFormulaWithBrickField(BrickField.TEXT),
                        getFormulaWithBrickField(BrickField.VALUE)));
    }
}
