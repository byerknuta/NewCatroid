package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtClipGradBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PtClipGradBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pt_clip_max_norm);
    }

    public PtClipGradBrick(float maxNorm) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(maxNorm));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_clip_grad;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createPtClipGradAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1)));
    }
}
