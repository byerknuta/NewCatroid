package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetFpsBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public SetFpsBrick() {
        addAllowedBrickField(BrickField.VALUE, R.id.brick_set_fps_edit_value);
    }

    public SetFpsBrick(int fps) {
        this(new Formula(fps));
    }

    public SetFpsBrick(Formula fpsFormula) {
        this();
        setFormulaWithBrickField(BrickField.VALUE, fpsFormula);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_fps;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createSetFpsAction(sprite, sequence, getFormulaWithBrickField(BrickField.VALUE)));
    }
}
