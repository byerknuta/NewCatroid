package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtLstmCellBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PtLstmCellBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_pt_lstm_name);
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pt_lstm_input);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_pt_lstm_hin);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_pt_lstm_cin);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_pt_lstm_hout);
        addAllowedBrickField(BrickField.VALUE_5, R.id.brick_pt_lstm_cout);
        addAllowedBrickField(BrickField.VALUE_6, R.id.brick_pt_lstm_indim);
        addAllowedBrickField(BrickField.VALUE_7, R.id.brick_pt_lstm_hiddendim);
    }

    public PtLstmCellBrick(String layerName, String input, String hIn, String cIn, String hOut, String cOut, int inDim, int hiddenDim) {
        this();
        setFormulaWithBrickField(BrickField.NAME, new Formula(layerName));
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(input));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(hIn));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(cIn));
        setFormulaWithBrickField(BrickField.VALUE_4, new Formula(hOut));
        setFormulaWithBrickField(BrickField.VALUE_5, new Formula(cOut));
        setFormulaWithBrickField(BrickField.VALUE_6, new Formula(inDim));
        setFormulaWithBrickField(BrickField.VALUE_7, new Formula(hiddenDim));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_lstm_cell;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createPtLstmCellAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.NAME),
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.VALUE_4),
                        getFormulaWithBrickField(BrickField.VALUE_5),
                        getFormulaWithBrickField(BrickField.VALUE_6),
                        getFormulaWithBrickField(BrickField.VALUE_7)));
    }
}
