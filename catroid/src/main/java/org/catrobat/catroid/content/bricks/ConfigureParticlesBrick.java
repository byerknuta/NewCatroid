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

public class ConfigureParticlesBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;
    private int configTypeSelection = 0;

    public ConfigureParticlesBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_http_configure_particles_id_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_http_configure_particles_value_field);
    }

    public ConfigureParticlesBrick(String particleId, int configTypeSelection, String value) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(particleId));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(value));
        this.configTypeSelection = configTypeSelection;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_http_configure_particles;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);

        Spinner spinner = view.findViewById(R.id.brick_http_configure_particles_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.brick_http_configure_particles_types, android.R.layout.simple_spinner_item);
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
                .createConfigureParticlesAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        configTypeSelection,
                        getFormulaWithBrickField(BrickField.VALUE_2)));
    }
}
