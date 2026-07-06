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

public class NativeViewAnimateBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;
    private int propertySelection = 0;
    private int easingSelection = 0;

    public NativeViewAnimateBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_id_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_value_field);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_duration_field);
    }

    public NativeViewAnimateBrick(String viewId, int propertySelection, double value, int durationMs, int easingSelection) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(viewId));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(value));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(durationMs));
        this.propertySelection = propertySelection;
        this.easingSelection = easingSelection;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_native_view_animate;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);

        Spinner propSpinner = view.findViewById(R.id.brick_property_spinner);
        ArrayAdapter<CharSequence> propAdapter = ArrayAdapter.createFromResource(context,
                R.array.native_view_animate_properties_array, android.R.layout.simple_spinner_item);
        propAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        propSpinner.setAdapter(propAdapter);
        propSpinner.setSelection(propertySelection);
        propSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { propertySelection = position; }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        Spinner easingSpinner = view.findViewById(R.id.brick_easing_spinner);
        ArrayAdapter<CharSequence> easingAdapter = ArrayAdapter.createFromResource(context,
                R.array.brick_easing_types, android.R.layout.simple_spinner_item);
        easingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        easingSpinner.setAdapter(easingAdapter);
        easingSpinner.setSelection(easingSelection);
        easingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { easingSelection = position; }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createNativeViewAnimateAction(sprite, sequence, propertySelection, easingSelection,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3)));
    }
}
