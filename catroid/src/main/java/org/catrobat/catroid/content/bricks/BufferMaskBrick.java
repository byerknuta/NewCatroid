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

public class BufferMaskBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;
    private int modeSelection = 0; // 0: Stretch, 1: Screen

    public BufferMaskBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_buffer_mask_name_field);
    }

    public BufferMaskBrick(String bufferName, int mode) {
        this(new Formula(bufferName), mode);
    }

    public BufferMaskBrick(Formula bufferName, int mode) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, bufferName);
        this.modeSelection = mode;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_buffer_mask;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);

        Spinner spinner = view.findViewById(R.id.brick_buffer_mask_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.brick_buffer_mask_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                modeSelection = position;
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinner.setSelection(modeSelection);
        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createBufferMaskAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        modeSelection));
    }
}
