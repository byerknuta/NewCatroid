package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetMaxPointLightsBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public SetMaxPointLightsBrick() {
        addAllowedBrickField(BrickField.MAX_LIGHTS, R.id.brick_max_lights_val);
    }

    public SetMaxPointLightsBrick(int limit) {
        this(new Formula(limit));
    }

    public SetMaxPointLightsBrick(Formula limit) {
        this();
        setFormulaWithBrickField(BrickField.MAX_LIGHTS, limit);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_max_point_lights;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createSetMaxPointLightsAction(
                sprite, sequence, getFormulaWithBrickField(BrickField.MAX_LIGHTS)
        ));
    }
}
