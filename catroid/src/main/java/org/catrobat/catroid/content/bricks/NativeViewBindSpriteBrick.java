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

public class NativeViewBindSpriteBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;
    private int alignMode = 1; // Default: Center

    public NativeViewBindSpriteBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_id_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_sprite_field);
        addAllowedBrickField(BrickField.X_POSITION, R.id.brick_x_field);
        addAllowedBrickField(BrickField.Y_POSITION, R.id.brick_y_field);
    }

    public NativeViewBindSpriteBrick(String viewId, String spriteName, int alignMode, int x, int y) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(viewId));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(spriteName));
        setFormulaWithBrickField(BrickField.X_POSITION, new Formula(x));
        setFormulaWithBrickField(BrickField.Y_POSITION, new Formula(y));
        this.alignMode = alignMode;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_native_view_bind;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);
        Spinner spinner = view.findViewById(R.id.brick_align_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.align_anchor_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(alignMode);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { alignMode = pos; }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createNativeViewBindSpriteAction(sprite, sequence, alignMode,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.X_POSITION),
                        getFormulaWithBrickField(BrickField.Y_POSITION)));
    }
}
