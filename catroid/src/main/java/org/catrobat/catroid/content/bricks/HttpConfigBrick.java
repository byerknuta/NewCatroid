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

public class HttpConfigBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;
    private int configTypeSelection = 0;

    public HttpConfigBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_http_config_id_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_http_config_key_field);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_http_config_value_field);
    }

    public HttpConfigBrick(String requestId, int configTypeSelection, String key, String value) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(requestId));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(key));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(value));
        this.configTypeSelection = configTypeSelection;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_http_config;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);

        Spinner typeSpinner = view.findViewById(R.id.brick_http_config_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.brick_http_config_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                configTypeSelection = position;
                updateKeyVisibility();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        typeSpinner.setSelection(configTypeSelection);
        updateKeyVisibility();

        return view;
    }

    private void updateKeyVisibility() {
        LinearLayout keyLayout = view.findViewById(R.id.brick_http_config_key_layout);
        if (keyLayout != null) {
            if (configTypeSelection == 0 || configTypeSelection == 1) {
                keyLayout.setVisibility(View.VISIBLE);
            } else {
                keyLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createHttpConfigAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        configTypeSelection,
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3)));
    }
}
