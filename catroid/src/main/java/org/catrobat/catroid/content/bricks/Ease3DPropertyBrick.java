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

public class Ease3DPropertyBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    private int propertySelectionIndex = 0;
    private int typeSelectionIndex = 0;

    public Ease3DPropertyBrick() {
        super();
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_ease_3d_property_object_id);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_ease_3d_property_start_edit);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_ease_3d_property_end_edit);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_ease_3d_property_duration_edit);
    }

    public Ease3DPropertyBrick(Formula objectId, int propertyIndex, int typeIndex, Formula start, Formula end, Formula duration) {
        this();
        this.propertySelectionIndex = propertyIndex;
        this.typeSelectionIndex = typeIndex;
        setFormulaWithBrickField(BrickField.VALUE_1, objectId);
        setFormulaWithBrickField(BrickField.VALUE_2, start);
        setFormulaWithBrickField(BrickField.VALUE_3, end);
        setFormulaWithBrickField(BrickField.VALUE_4, duration);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_ease_3d_property;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);

        Spinner propertySpinner = view.findViewById(R.id.ease_3d_property_target_spinner);
        ArrayAdapter<CharSequence> propertyAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.brick_ease_3d_properties,
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

        Spinner typeSpinner = view.findViewById(R.id.ease_3d_property_type_spinner);
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
        sequence.addAction(sprite.getActionFactory().createEase3DPropertyAction(
                sprite,
                sequence,
                getFormulaWithBrickField(BrickField.VALUE_1),
                propertySelectionIndex,
                typeSelectionIndex,
                getFormulaWithBrickField(BrickField.VALUE_4),
                getFormulaWithBrickField(BrickField.VALUE_2),
                getFormulaWithBrickField(BrickField.VALUE_3)
        ));
    }
}
