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

public class ConfigureLightBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;
    private int configTypeSelection = 0;

    public ConfigureLightBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_http_configure_light_id_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_http_configure_light_value_field);
    }

    public ConfigureLightBrick(String lightId, int configTypeSelection, String value) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(lightId));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(value));
        this.configTypeSelection = configTypeSelection;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_http_configure_light;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);

        Spinner spinner = view.findViewById(R.id.brick_http_configure_light_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.brick_http_configure_light_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                configTypeSelection = position;
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinner.setSelection(configTypeSelection);
        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createConfigureLightAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        configTypeSelection,
                        getFormulaWithBrickField(BrickField.VALUE_2)));
    }
}
