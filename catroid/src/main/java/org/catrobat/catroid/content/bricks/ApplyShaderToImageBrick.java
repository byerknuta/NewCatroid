package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class ApplyShaderToImageBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public ApplyShaderToImageBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_apply_shader_filename);
        addAllowedBrickField(BrickField.TEXT, R.id.brick_apply_shader_vsh);
        addAllowedBrickField(BrickField.X_POSITION, R.id.brick_apply_shader_fsh);
    }

    public ApplyShaderToImageBrick(String filename, String vsh, String fsh) {
        this(new Formula(filename), new Formula(vsh), new Formula(fsh));
    }

    public ApplyShaderToImageBrick(Formula filename, Formula vsh, Formula fsh) {
        this();
        setFormulaWithBrickField(BrickField.NAME, filename);
        setFormulaWithBrickField(BrickField.TEXT, vsh);
        setFormulaWithBrickField(BrickField.X_POSITION, fsh);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_apply_shader_to_image;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createApplyShaderToImageAction(
                sprite, sequence,
                getFormulaWithBrickField(BrickField.NAME),
                getFormulaWithBrickField(BrickField.TEXT),
                getFormulaWithBrickField(BrickField.X_POSITION)
        ));
    }
}
