package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class ThreedCreateCylinderBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public ThreedCreateCylinderBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_threed_create_cylinder_id);
    }

    public ThreedCreateCylinderBrick(String id) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(id));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_threed_create_cylinder;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createThreedCreateCylinderAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1)));
    }
}