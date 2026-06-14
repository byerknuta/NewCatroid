package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class DetachFromCameraBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public DetachFromCameraBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_detach_camera_object_name);
    }

    public DetachFromCameraBrick(String objectName) {
        this(new Formula(objectName));
    }

    public DetachFromCameraBrick(Formula objectName) {
        this();
        setFormulaWithBrickField(BrickField.NAME, objectName);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_detach_from_camera;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createDetachFromCameraAction(
                sprite, sequence, getFormulaWithBrickField(BrickField.NAME)
        ));
    }
}
