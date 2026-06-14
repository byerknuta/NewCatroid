package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class Set3DSoundMaxDistanceBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public Set3DSoundMaxDistanceBrick() {
        addAllowedBrickField(BrickField.INSTANCE_NAME, R.id.brick_set_3d_sound_max_distance_instance);
        addAllowedBrickField(BrickField.DISTANCE, R.id.brick_set_3d_sound_max_distance_val);
    }

    public Set3DSoundMaxDistanceBrick(String instanceName, float distance) {
        this(new Formula(instanceName), new Formula(distance));
    }

    public Set3DSoundMaxDistanceBrick(Formula instanceName, Formula distance) {
        this();
        setFormulaWithBrickField(BrickField.INSTANCE_NAME, instanceName);
        setFormulaWithBrickField(BrickField.DISTANCE, distance);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_3d_sound_max_distance;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createSet3DSoundMaxDistanceAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.INSTANCE_NAME),
                        getFormulaWithBrickField(BrickField.DISTANCE)));
    }
}
