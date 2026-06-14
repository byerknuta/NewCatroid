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

public class SetPostProcessingNewBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;
    private int effectIndex = 0;
    private int paramIndex = 0;

    public SetPostProcessingNewBrick() {
        addAllowedBrickField(BrickField.VALUE, R.id.brick_pp_value_edit);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_post_processing_new;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);
        Spinner effectSpinner = view.findViewById(R.id.brick_pp_effect_spinner);
        Spinner paramSpinner = view.findViewById(R.id.brick_pp_param_spinner);

        ArrayAdapter<CharSequence> effectAdapter = ArrayAdapter.createFromResource(context,
                R.array.pp_new_effect_types, android.R.layout.simple_spinner_item);
        effectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        effectSpinner.setAdapter(effectAdapter);
        effectSpinner.setSelection(effectIndex);

        effectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                boolean changed = (effectIndex != position);
                effectIndex = position;
                updateParamSpinner(context, paramSpinner, position);
                if (changed) {
                    paramIndex = 0;
                    paramSpinner.setSelection(0);
                } else {
                    paramSpinner.setSelection(paramIndex);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        paramSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                paramIndex = position;
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private void updateParamSpinner(Context context, Spinner paramSpinner, int effectIdx) {
        int arrayRes;
        switch (effectIdx) {
            case 0: arrayRes = R.array.pp_params_global; break;
            case 1: arrayRes = R.array.pp_params_bloom; break;
            case 2: arrayRes = R.array.pp_params_vignette; break;
            case 3: arrayRes = R.array.pp_params_levels; break;
            case 4: arrayRes = R.array.pp_params_grain; break;
            case 5: arrayRes = R.array.pp_params_fxaa; break;
            case 6: arrayRes = R.array.pp_params_chromatic; break;
            case 7: arrayRes = R.array.pp_params_radial; break;
            case 8: arrayRes = R.array.pp_params_oldtv; break;
            case 9: arrayRes = R.array.pp_params_crt; break;
            case 10: arrayRes = R.array.pp_params_fisheye; break;
            case 11: arrayRes = R.array.pp_params_water; break;
            case 12: arrayRes = R.array.pp_params_motionblur; break;
            case 13: arrayRes = R.array.pp_params_lensflare; break;
            case 14: arrayRes = R.array.pp_params_gaussian; break;
            case 15: arrayRes = R.array.pp_params_zoom; break;
            case 16: arrayRes = R.array.pp_params_aces; break;
            case 17: arrayRes = R.array.pp_params_eyeadapt; break;
            case 18: arrayRes = R.array.pp_params_ssr; break;
            case 19: arrayRes = R.array.pp_params_ssao; break;
            case 20: arrayRes = R.array.pp_params_heightfog; break;
            case 21: arrayRes = R.array.pp_params_dof; break;
            case 22: arrayRes = R.array.pp_params_godrays; break;
            case 23: arrayRes = R.array.pp_params_volfog; break;
            case 24: arrayRes = R.array.pp_params_fsr; break;
            case 25: arrayRes = R.array.pp_params_ssgi; break;
            default: arrayRes = R.array.pp_params_global;
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                arrayRes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paramSpinner.setAdapter(adapter);
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createSetPostProcessingNewAction(sprite, sequence,
                        effectIndex, paramIndex, getFormulaWithBrickField(BrickField.VALUE)));
    }
}
