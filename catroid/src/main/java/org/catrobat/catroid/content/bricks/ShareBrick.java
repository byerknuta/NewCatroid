package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class ShareBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public ShareBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_share_edit_text);
    }

    public ShareBrick(String content) {
        this(new Formula(content));
    }

    public ShareBrick(Formula contentFormula) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, contentFormula);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_share;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createShareAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1)));
    }
}
