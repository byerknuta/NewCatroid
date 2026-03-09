package org.catrobat.catroid.content.bricks;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class ThreedAlignNormalBrick extends FormulaBrick {
    public ThreedAlignNormalBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_align_id);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_align_nx);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_align_ny);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_align_nz);
    }
    public ThreedAlignNormalBrick(String id, double x, double y, double z) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(id));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(x));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(y));
        setFormulaWithBrickField(BrickField.VALUE_4, new Formula(z));
    }
    @Override public int getViewResource() { return R.layout.brick_threed_align_normal; }
    @Override public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createThreedAlignNormalAction(sprite, sequence,
                getFormulaWithBrickField(BrickField.VALUE_1), getFormulaWithBrickField(BrickField.VALUE_2),
                getFormulaWithBrickField(BrickField.VALUE_3), getFormulaWithBrickField(BrickField.VALUE_4)));
    }
}