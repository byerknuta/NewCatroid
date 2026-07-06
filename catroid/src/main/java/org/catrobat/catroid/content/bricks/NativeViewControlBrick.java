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

public class NativeViewControlBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;
    private int commandSelection = 0;

    public NativeViewControlBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_id_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_param_field);
    }

    public NativeViewControlBrick(String viewId, int commandSelection, String parameter) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(viewId));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(parameter));
        this.commandSelection = commandSelection;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_native_view_control;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);
        Spinner spinner = view.findViewById(R.id.brick_command_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.native_view_commands_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(commandSelection);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                commandSelection = position;
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createNativeViewControlAction(sprite, sequence, commandSelection,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2)));
    }
}
