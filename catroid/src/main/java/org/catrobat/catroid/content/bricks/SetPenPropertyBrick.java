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

public class SetPenPropertyBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;
    private int propertySelection = 0;

    public SetPenPropertyBrick() {
        addAllowedBrickField(BrickField.VALUE, R.id.brick_set_pen_property_value);
    }

    public SetPenPropertyBrick(int propertySelection, String value) {
        this(propertySelection, new Formula(value));
    }

    public SetPenPropertyBrick(int propertySelection, Formula valueFormula) {
        this();
        setFormulaWithBrickField(BrickField.VALUE, valueFormula);
        this.propertySelection = propertySelection;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_pen_property;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);

        Spinner spinner = view.findViewById(R.id.brick_set_pen_property_spinner);
        if (spinner != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    context, R.array.brick_pen_properties, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    propertySelection = position;
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });

            spinner.setSelection(propertySelection);
        }

        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createSetPenPropertyAction(sprite, sequence,
                        propertySelection,
                        getFormulaWithBrickField(BrickField.VALUE)));
    }
}
