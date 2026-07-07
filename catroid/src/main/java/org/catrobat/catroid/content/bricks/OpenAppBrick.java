package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class OpenAppBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public OpenAppBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_open_app_pkg_field);
    }

    public OpenAppBrick(String packageName) {
        this(new Formula(packageName));
    }

    public OpenAppBrick(Formula packageName) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, packageName);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_open_app;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createOpenAppAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1)));
    }
}
