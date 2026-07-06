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

public class NativeViewListenerBrick extends UserVariableBrickWithFormula {
    private static final long serialVersionUID = 1L;
    private int eventSelection = 0;

    public NativeViewListenerBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_id_field);
    }

    public NativeViewListenerBrick(String viewId, int eventSelection, UserVariable userVariable) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(viewId));
        this.userVariable = userVariable;
        this.eventSelection = eventSelection;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_native_view_listener;
    }

    @Override
    protected int getSpinnerId() {
        return R.id.brick_variable_spinner;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);
        Spinner spinner = view.findViewById(R.id.brick_event_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.native_view_events_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(eventSelection);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                eventSelection = position;
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createNativeViewListenerAction(sprite, sequence, eventSelection,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        userVariable));
    }
}
