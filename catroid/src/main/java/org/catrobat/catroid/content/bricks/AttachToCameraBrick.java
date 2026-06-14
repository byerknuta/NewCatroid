package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class AttachToCameraBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public AttachToCameraBrick() {
        addAllowedBrickField(BrickField.NAME, R.id.brick_attach_camera_object_name);
    }

    public AttachToCameraBrick(String objectName) {
        this(new Formula(objectName));
    }

    public AttachToCameraBrick(Formula objectName) {
        this();
        setFormulaWithBrickField(BrickField.NAME, objectName);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_attach_to_camera;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createAttachToCameraAction(
                sprite, sequence, getFormulaWithBrickField(BrickField.NAME)
        ));
    }
}
