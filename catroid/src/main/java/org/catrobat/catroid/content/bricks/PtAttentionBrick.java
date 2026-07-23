package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtAttentionBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PtAttentionBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_pt_attn_name);
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pt_attn_input);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_pt_attn_output);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_pt_attn_dim);
    }

    public PtAttentionBrick(String layerName, String input, String output, int embedDim) {
        this();
        setFormulaWithBrickField(BrickField.NAME, new Formula(layerName));
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(input));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(output));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(embedDim));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_attention;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createPtAttentionAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.NAME),
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3)));
    }
}
