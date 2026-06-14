package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetRenderResolutionBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public SetRenderResolutionBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_set_render_res_value);
        addAllowedBrickField(BrickField.VALUE, R.id.brick_set_render_aspect_value);
    }

    public SetRenderResolutionBrick(float scale, int mode) {
        this(new Formula(scale), new Formula(mode));
    }

    public SetRenderResolutionBrick(Formula scaleFormula, Formula modeFormula) {
        this();
        setFormulaWithBrickField(BrickField.NAME, scaleFormula);
        setFormulaWithBrickField(BrickField.VALUE, modeFormula);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_render_resolution;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createSetRenderResolutionAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.NAME), getFormulaWithBrickField(BrickField.VALUE)));
    }
}
