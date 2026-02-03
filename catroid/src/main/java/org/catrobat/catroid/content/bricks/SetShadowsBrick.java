package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetShadowsBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public SetShadowsBrick() {
        addAllowedBrickField(Brick.BrickField.TEXT, R.id.brick_set_shadows_edit_text);
    }

    public SetShadowsBrick(String value) {
        this(new Formula(value));
    }

    public SetShadowsBrick(Formula formula) {
        this();
        setFormulaWithBrickField(BrickField.TEXT, formula);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_shadows;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createSetShadowsAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.TEXT)));
    }
}