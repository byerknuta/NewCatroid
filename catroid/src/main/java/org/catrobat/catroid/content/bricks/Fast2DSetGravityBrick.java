package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class Fast2DSetGravityBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public Fast2DSetGravityBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_fast2d_set_gravity_x);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_fast2d_set_gravity_y);
    }

    public Fast2DSetGravityBrick(float gx, float gy) {
        this(new Formula(gx), new Formula(gy));
    }

    public Fast2DSetGravityBrick(Formula gxFormula, Formula gyFormula) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, gxFormula);
        setFormulaWithBrickField(BrickField.VALUE_2, gyFormula);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_fast2d_set_gravity;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createFast2DSetGravityAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2)));
    }
}
