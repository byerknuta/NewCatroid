package org.catrobat.catroid.content.bricks;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class ThreedCreateFixedConstraintBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    private int modeSelection = 0; // 0 = Auto, 1 = Manual

    public ThreedCreateFixedConstraintBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_fixed_joint_id);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_fixed_joint_a);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_fixed_joint_b);

        addAllowedBrickField(BrickField.X, R.id.brick_fixed_joint_ax);
        addAllowedBrickField(BrickField.Y, R.id.brick_fixed_joint_ay);
        addAllowedBrickField(BrickField.Z, R.id.brick_fixed_joint_az);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_fixed_joint_bx);
        addAllowedBrickField(BrickField.VALUE_5, R.id.brick_fixed_joint_by);
        addAllowedBrickField(BrickField.VALUE_6, R.id.brick_fixed_joint_bz);
    }

    public ThreedCreateFixedConstraintBrick(String id, String a, String b) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(id));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(a));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(b));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_threed_create_fixed_constraint;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);

        Spinner spinner = view.findViewById(R.id.brick_fixed_joint_spinner);
        LinearLayout manualLayout = view.findViewById(R.id.brick_fixed_joint_manual_layout);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.brick_threed_joint_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                modeSelection = position;
                manualLayout.setVisibility(modeSelection == 1 ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinner.setSelection(modeSelection);
        manualLayout.setVisibility(modeSelection == 1 ? View.VISIBLE : View.GONE);

        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        var action = sprite.getActionFactory().createThreedCreateFixedConstraintAction(
                sprite, sequence,
                getFormulaWithBrickField(BrickField.VALUE_1), // ID
                getFormulaWithBrickField(BrickField.VALUE_2), // A
                getFormulaWithBrickField(BrickField.VALUE_3)  // B
        );

        action.mode = this.modeSelection;

        action.ax = getFormulaWithBrickField(BrickField.X);
        action.ay = getFormulaWithBrickField(BrickField.Y);
        action.az = getFormulaWithBrickField(BrickField.Z);
        action.bx = getFormulaWithBrickField(BrickField.VALUE_4);
        action.by = getFormulaWithBrickField(BrickField.VALUE_5);
        action.bz = getFormulaWithBrickField(BrickField.VALUE_6);

        sequence.addAction(action);
    }
}