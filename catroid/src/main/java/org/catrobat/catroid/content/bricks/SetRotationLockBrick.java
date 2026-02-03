package org.catrobat.catroid.content.bricks;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.content.actions.SetRotationLockAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetRotationLockBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public boolean lockX = false;
    public boolean lockY = false;
    public boolean lockZ = false;

    public SetRotationLockBrick() {
        addAllowedBrickField(BrickField.STRING, R.id.brick_rotation_lock_object_id);
    }

    public SetRotationLockBrick(String objectId, boolean x, boolean y, boolean z) {
        this();
        setFormulaWithBrickField(BrickField.STRING, new Formula(objectId));
        this.lockX = x;
        this.lockY = y;
        this.lockZ = z;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_rotation_lock;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);

        CheckBox cbX = view.findViewById(R.id.brick_rotation_lock_x);
        CheckBox cbY = view.findViewById(R.id.brick_rotation_lock_y);
        CheckBox cbZ = view.findViewById(R.id.brick_rotation_lock_z);

        cbX.setChecked(lockX);
        cbY.setChecked(lockY);
        cbZ.setChecked(lockZ);

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            int id = buttonView.getId();
            if (id == R.id.brick_rotation_lock_x) lockX = isChecked;
            else if (id == R.id.brick_rotation_lock_y) lockY = isChecked;
            else if (id == R.id.brick_rotation_lock_z) lockZ = isChecked;
        };

        cbX.setOnCheckedChangeListener(listener);
        cbY.setOnCheckedChangeListener(listener);
        cbZ.setOnCheckedChangeListener(listener);

        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        SetRotationLockAction action = (SetRotationLockAction) sprite.getActionFactory()
                .createSetRotationLockAction(sprite, sequence, getFormulaWithBrickField(BrickField.STRING));

        action.setLockX(lockX);
        action.setLockY(lockY);
        action.setLockZ(lockZ);

        sequence.addAction(action);
    }
}