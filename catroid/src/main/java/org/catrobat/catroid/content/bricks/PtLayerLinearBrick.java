package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PtLayerLinearBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PtLayerLinearBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_pt_linear_layer_name);
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_pt_linear_input);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_pt_linear_output);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_pt_linear_in_f);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_pt_linear_out_f);
    }

    public PtLayerLinearBrick(String layerName, String input, String output, int inFeatures, int outFeatures) {
        this();
        setFormulaWithBrickField(BrickField.NAME, new Formula(layerName));
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(input));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(output));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(inFeatures));
        setFormulaWithBrickField(BrickField.VALUE_4, new Formula(outFeatures));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_pt_layer_linear;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createPtLayerLinearAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.NAME),
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.VALUE_4)));
    }
}
