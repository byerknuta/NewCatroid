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

public class EasePropertyBrick extends FormulaBrick {

    private static final long serialVersionUID = 1L;

    private int propertySelectionIndex = 0;
    private int typeSelectionIndex = 0;

    public EasePropertyBrick() {
        super();
        addAllowedBrickField(BrickField.IF_CONDITION, R.id.brick_ease_property_start_edit);
        addAllowedBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, R.id.brick_ease_property_end_edit);
        addAllowedBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, R.id.brick_ease_property_duration_edit);
    }

    public EasePropertyBrick(int propertyIndex, int typeIndex, float start, float end, float duration) {
        this();
        this.propertySelectionIndex = propertyIndex;
        this.typeSelectionIndex = typeIndex;
        setFormulaWithBrickField(BrickField.IF_CONDITION, new Formula(start));
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, new Formula(end));
        setFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, new Formula(duration));
    }

    public EasePropertyBrick(int propertyIndex, int typeIndex, Formula start, Formula end, Formula duration) {
        this();
        this.propertySelectionIndex = propertyIndex;
        this.typeSelectionIndex = typeIndex;
        setFormulaWithBrickField(BrickField.IF_CONDITION, start);
        setFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE, end);
        setFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE, duration);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_ease_property;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);

        Spinner propertySpinner = view.findViewById(R.id.ease_property_target_spinner);
        ArrayAdapter<CharSequence> propertyAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.brick_ease_properties,
                android.R.layout.simple_spinner_item
        );
        propertyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        propertySpinner.setAdapter(propertyAdapter);

        propertySpinner.setSelection(propertySelectionIndex);
        propertySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                propertySelectionIndex = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Spinner typeSpinner = view.findViewById(R.id.ease_property_type_spinner);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.brick_easing_types,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        typeSpinner.setSelection(typeSelectionIndex);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                typeSelectionIndex = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createEasePropertyAction(
                sprite,
                sequence,
                propertySelectionIndex,
                typeSelectionIndex,
                getFormulaWithBrickField(BrickField.REPLACE_ITEM_IN_USERLIST_VALUE),
                getFormulaWithBrickField(BrickField.IF_CONDITION),
                getFormulaWithBrickField(BrickField.INSERT_ITEM_INTO_USERLIST_VALUE)
        ));
    }
}
