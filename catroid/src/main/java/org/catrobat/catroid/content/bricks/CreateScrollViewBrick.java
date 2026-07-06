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

public class CreateScrollViewBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;
    private int scrollMode = 1;
    private int overScrollMode = 1;

    public CreateScrollViewBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_id_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_bg_field);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_bars_field);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_padding_field);
        addAllowedBrickField(BrickField.X_POSITION, R.id.brick_x_field);
        addAllowedBrickField(BrickField.Y_POSITION, R.id.brick_y_field);
        addAllowedBrickField(BrickField.WIDTH, R.id.brick_w_field);
        addAllowedBrickField(BrickField.HEIGHT, R.id.brick_h_field);
    }

    public CreateScrollViewBrick(String viewId, int mode, String bg, boolean showBars, int padding, int overScroll, int x, int y, int w, int h) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(viewId));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(bg));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(showBars));
        setFormulaWithBrickField(BrickField.VALUE_4, new Formula(padding));
        setFormulaWithBrickField(BrickField.X_POSITION, new Formula(x));
        setFormulaWithBrickField(BrickField.Y_POSITION, new Formula(y));
        setFormulaWithBrickField(BrickField.WIDTH, new Formula(w));
        setFormulaWithBrickField(BrickField.HEIGHT, new Formula(h));
        this.scrollMode = mode;
        this.overScrollMode = overScroll;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_create_scroll_view;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);

        Spinner modeSpinner = view.findViewById(R.id.brick_mode_spinner);
        ArrayAdapter<CharSequence> modeAdapter = ArrayAdapter.createFromResource(context,
                R.array.scroll_modes_array, android.R.layout.simple_spinner_item);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(modeAdapter);
        modeSpinner.setSelection(scrollMode);
        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { scrollMode = pos; }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        Spinner overSpinner = view.findViewById(R.id.brick_overscroll_spinner);
        ArrayAdapter<CharSequence> overAdapter = ArrayAdapter.createFromResource(context,
                R.array.overscroll_modes_array, android.R.layout.simple_spinner_item);
        overAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        overSpinner.setAdapter(overAdapter);
        overSpinner.setSelection(overScrollMode);
        overSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { overScrollMode = pos; }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createCreateScrollViewAction(sprite, sequence, scrollMode, overScrollMode,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.VALUE_4),
                        getFormulaWithBrickField(BrickField.X_POSITION),
                        getFormulaWithBrickField(BrickField.Y_POSITION),
                        getFormulaWithBrickField(BrickField.WIDTH),
                        getFormulaWithBrickField(BrickField.HEIGHT)));
    }
}
