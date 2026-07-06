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

public class CreateTextLabelBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;
    private int formatMode = 0;
    private int alignMode = 0;
    private int scrollMode = 0;

    public CreateTextLabelBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_id_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_text_field);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_color_field);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_size_field);
        addAllowedBrickField(BrickField.VALUE_5, R.id.brick_bg_field);
        addAllowedBrickField(BrickField.VALUE_6, R.id.brick_font_field);
        addAllowedBrickField(BrickField.VALUE_7, R.id.brick_radius_field);
        addAllowedBrickField(BrickField.VALUE_8, R.id.brick_spacing_field);
        addAllowedBrickField(BrickField.X_POSITION, R.id.brick_x_field);
        addAllowedBrickField(BrickField.Y_POSITION, R.id.brick_y_field);
        addAllowedBrickField(BrickField.WIDTH, R.id.brick_w_field);
        addAllowedBrickField(BrickField.HEIGHT, R.id.brick_h_field);
    }

    public CreateTextLabelBrick(String viewId, String text, String color, double size, String bg, String font, int radius, double spacing, int format, int align, int scroll, int x, int y, int w, int h) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(viewId));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(text));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(color));
        setFormulaWithBrickField(BrickField.VALUE_4, new Formula(size));
        setFormulaWithBrickField(BrickField.VALUE_5, new Formula(bg));
        setFormulaWithBrickField(BrickField.VALUE_6, new Formula(font));
        setFormulaWithBrickField(BrickField.VALUE_7, new Formula(radius));
        setFormulaWithBrickField(BrickField.VALUE_8, new Formula(spacing));
        setFormulaWithBrickField(BrickField.X_POSITION, new Formula(x));
        setFormulaWithBrickField(BrickField.Y_POSITION, new Formula(y));
        setFormulaWithBrickField(BrickField.WIDTH, new Formula(w));
        setFormulaWithBrickField(BrickField.HEIGHT, new Formula(h));
        this.formatMode = format;
        this.alignMode = align;
        this.scrollMode = scroll;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_create_text_label;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);

        Spinner formatSpinner = view.findViewById(R.id.brick_format_spinner);
        ArrayAdapter<CharSequence> formatAdapter = ArrayAdapter.createFromResource(context, R.array.text_formats_array, android.R.layout.simple_spinner_item);
        formatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(formatAdapter);
        formatSpinner.setSelection(formatMode);
        formatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { formatMode = pos; }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        Spinner alignSpinner = view.findViewById(R.id.brick_align_spinner);
        ArrayAdapter<CharSequence> alignAdapter = ArrayAdapter.createFromResource(context, R.array.align_modes_array, android.R.layout.simple_spinner_item);
        alignAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        alignSpinner.setAdapter(alignAdapter);
        alignSpinner.setSelection(alignMode);
        alignSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { alignMode = pos; }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        Spinner scrollSpinner = view.findViewById(R.id.brick_scroll_spinner);
        ArrayAdapter<CharSequence> scrollAdapter = ArrayAdapter.createFromResource(context, R.array.scroll_modes_array, android.R.layout.simple_spinner_item);
        scrollAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scrollSpinner.setAdapter(scrollAdapter);
        scrollSpinner.setSelection(scrollMode);
        scrollSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { scrollMode = pos; }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createCreateTextLabelAction(sprite, sequence, formatMode, alignMode, scrollMode,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.VALUE_4),
                        getFormulaWithBrickField(BrickField.VALUE_5),
                        getFormulaWithBrickField(BrickField.VALUE_6),
                        getFormulaWithBrickField(BrickField.VALUE_7),
                        getFormulaWithBrickField(BrickField.VALUE_8),
                        getFormulaWithBrickField(BrickField.X_POSITION),
                        getFormulaWithBrickField(BrickField.Y_POSITION),
                        getFormulaWithBrickField(BrickField.WIDTH),
                        getFormulaWithBrickField(BrickField.HEIGHT)));
    }
}
