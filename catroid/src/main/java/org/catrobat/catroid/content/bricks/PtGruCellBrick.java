package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtGruCellBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PtGruCellBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_pt_gru_name);
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pt_gru_input);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_pt_gru_hin);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_pt_gru_hout);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_pt_gru_indim);
        addAllowedBrickField(BrickField.VALUE_5, R.id.brick_pt_gru_hiddendim);
    }

    public PtGruCellBrick(String layerName, String input, String hIn, String hOut, int inDim, int hiddenDim) {
        this();
        setFormulaWithBrickField(BrickField.NAME, new Formula(layerName));
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(input));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(hIn));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(hOut));
        setFormulaWithBrickField(BrickField.VALUE_4, new Formula(inDim));
        setFormulaWithBrickField(BrickField.VALUE_5, new Formula(hiddenDim));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_gru_cell;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createPtGruCellAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.NAME),
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.VALUE_4),
                        getFormulaWithBrickField(BrickField.VALUE_5)));
    }
}
