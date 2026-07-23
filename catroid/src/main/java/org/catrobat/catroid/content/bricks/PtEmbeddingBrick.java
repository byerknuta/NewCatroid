package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtEmbeddingBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PtEmbeddingBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_pt_emb_name);
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pt_emb_input);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_pt_emb_output);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_pt_emb_vocab);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_pt_emb_dim);
    }

    public PtEmbeddingBrick(String layerName, String input, String output, int vocabSize, int embDim) {
        this();
        setFormulaWithBrickField(BrickField.NAME, new Formula(layerName));
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(input));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(output));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(vocabSize));
        setFormulaWithBrickField(BrickField.VALUE_4, new Formula(embDim));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_embedding;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createPtEmbeddingAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.NAME),
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.VALUE_4)));
    }
}
