package org.catrobat.catroid.content.bricks;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.UserVariable;

public class SetVariableEasingBrick extends UserVariableBrickWithFormula {

    private static final long serialVersionUID = 1L;


    private int typeSelectionIndex = 0;

    public SetVariableEasingBrick() {
        super();

        addAllowedBrickField(BrickField.IF_CONDITION, R.id.brick_easing_time_edit);
        addAllowedBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, R.id.brick_easing_duration_edit);
        addAllowedBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, R.id.brick_easing_start_edit);
        addAllowedBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_INDEX, R.id.brick_easing_end_edit);
    }

    public SetVariableEasingBrick(UserVariable variable, int typeIndex, float time, float duration, float start, float end) {
        this();
        this.userVariable = variable;
        this.typeSelectionIndex = typeIndex;
        setFormulaWithBrickField(BrickField.IF_CONDITION, new Formula(time));
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, new Formula(duration));
        setFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, new Formula(start));
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_INDEX, new Formula(end));
    }

    public SetVariableEasingBrick(int typeIndex, float time, float duration, float start, float end) {
        this();
        this.typeSelectionIndex = typeIndex;
        setFormulaWithBrickField(BrickField.IF_CONDITION, new Formula(time));
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, new Formula(duration));
        setFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, new Formula(start));
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_INDEX, new Formula(end));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_variable_easing;
    }

    @Override
    protected int getSpinnerId() {

        return R.id.set_variable_easing_variable_spinner;
    }

    @Override
    public View getView(Context context) {

        super.getView(context);


        Spinner typeSpinner = view.findViewById(R.id.set_variable_easing_type_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                context,
                R.array.brick_easing_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);


        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                typeSelectionIndex = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        typeSpinner.setSelection(typeSelectionIndex);

        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createSetVariableEasingAction(
                sprite,
                sequence,
                userVariable,
                typeSelectionIndex,
                getFormulaWithBrickField(BrickField.IF_CONDITION),
                getFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE),
                getFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE),
                getFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_INDEX)
        ));
    }
}