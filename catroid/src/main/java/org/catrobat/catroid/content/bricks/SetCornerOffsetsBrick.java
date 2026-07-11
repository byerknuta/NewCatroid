package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetCornerOffsetsBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public SetCornerOffsetsBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_set_corner_offsets_tlx);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_set_corner_offsets_tly);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_set_corner_offsets_trx);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_set_corner_offsets_try);
        addAllowedBrickField(BrickField.VALUE_5, R.id.brick_set_corner_offsets_brx);
        addAllowedBrickField(BrickField.VALUE_6, R.id.brick_set_corner_offsets_bry);
        addAllowedBrickField(BrickField.VALUE_7, R.id.brick_set_corner_offsets_blx);
        addAllowedBrickField(BrickField.VALUE_8, R.id.brick_set_corner_offsets_bly);
    }

    public SetCornerOffsetsBrick(Formula tlx, Formula tly, Formula trx, Formula tryF, Formula brx, Formula bry, Formula blx, Formula bly) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, tlx);
        setFormulaWithBrickField(BrickField.VALUE_2, tly);
        setFormulaWithBrickField(BrickField.VALUE_3, trx);
        setFormulaWithBrickField(BrickField.VALUE_4, tryF);
        setFormulaWithBrickField(BrickField.VALUE_5, brx);
        setFormulaWithBrickField(BrickField.VALUE_6, bry);
        setFormulaWithBrickField(BrickField.VALUE_7, blx);
        setFormulaWithBrickField(BrickField.VALUE_8, bly);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_corner_offsets;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createSetCornerOffsetsAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.VALUE_4),
                        getFormulaWithBrickField(BrickField.VALUE_5),
                        getFormulaWithBrickField(BrickField.VALUE_6),
                        getFormulaWithBrickField(BrickField.VALUE_7),
                        getFormulaWithBrickField(BrickField.VALUE_8)));
    }
}
