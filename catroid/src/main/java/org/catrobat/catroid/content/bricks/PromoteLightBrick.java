package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PromoteLightBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PromoteLightBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_promote_light_id_val);
    }

    public PromoteLightBrick(String lightId) {
        this(new Formula(lightId));
    }

    public PromoteLightBrick(Formula lightId) {
        this();
        setFormulaWithBrickField(BrickField.NAME, lightId);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_promote_light;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createPromoteLightAction(
                sprite, sequence, getFormulaWithBrickField(BrickField.NAME)
        ));
    }
}
