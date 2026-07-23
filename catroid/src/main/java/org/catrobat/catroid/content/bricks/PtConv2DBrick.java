package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtConv2DBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PtConv2DBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_pt_conv_name);
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pt_conv_input);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_pt_conv_output);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_pt_conv_in_c);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_pt_conv_out_c);
        addAllowedBrickField(BrickField.VALUE_5, R.id.brick_pt_conv_ksize);
        addAllowedBrickField(BrickField.VALUE_6, R.id.brick_pt_conv_stride);
    }

    public PtConv2DBrick(String layerName, String input, String output, int inC, int outC, int kSize, int stride) {
        this();
        setFormulaWithBrickField(BrickField.NAME, new Formula(layerName));
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(input));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(output));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(inC));
        setFormulaWithBrickField(BrickField.VALUE_4, new Formula(outC));
        setFormulaWithBrickField(BrickField.VALUE_5, new Formula(kSize));
        setFormulaWithBrickField(BrickField.VALUE_6, new Formula(stride));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_conv2d;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createPtConv2DAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.NAME),
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.VALUE_4),
                        getFormulaWithBrickField(BrickField.VALUE_5),
                        getFormulaWithBrickField(BrickField.VALUE_6)));
    }
}
