package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PlayGifBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PlayGifBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_play_gif_edit);
    }

    public PlayGifBrick(String filename) {
        this(new Formula(filename));
    }

    public PlayGifBrick(Formula filenameFormula) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, filenameFormula);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_play_gif;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createPlayGifAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1)));
    }
}
