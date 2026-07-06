package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class EnableBackgroundModeBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public EnableBackgroundModeBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_enable_background_mode_edit_title);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_enable_background_mode_edit_text);
    }

    public EnableBackgroundModeBrick(String title, String text) {
        this(new Formula(title), new Formula(text));
    }

    public EnableBackgroundModeBrick(Formula title, Formula text) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, title);
        setFormulaWithBrickField(BrickField.VALUE_2, text);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_enable_background_mode;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createEnableBackgroundModeAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2)));
    }
}
