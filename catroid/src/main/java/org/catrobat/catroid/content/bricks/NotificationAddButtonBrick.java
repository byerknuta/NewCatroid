package org.catrobat.catroid.content.bricks;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.LinearLayout;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class NotificationAddButtonBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;
    private int behavior = 0;
    private boolean hasInput = false;
    private boolean autoClose = false;

    public NotificationAddButtonBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_notif_btn_id);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_notif_btn_action);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_notif_btn_text);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_notif_btn_icon);
        addAllowedBrickField(BrickField.VALUE_5, R.id.brick_notif_btn_hint);
    }

    public NotificationAddButtonBrick(String notifId, String actionId, String text, String icon, String hint) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(notifId));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(actionId));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(text));
        setFormulaWithBrickField(BrickField.VALUE_4, new Formula(icon));
        setFormulaWithBrickField(BrickField.VALUE_5, new Formula(hint));
    }

    @Override
    public int getViewResource() { return R.layout.brick_notification_add_button; }

    @Override
    public View getView(Context context) {
        super.getView(context);

        Spinner spinner = view.findViewById(R.id.brick_notif_btn_spinner_behavior);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.brick_notification_behaviors, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) { behavior = pos; }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinner.setSelection(behavior);

        LinearLayout hintLayout = view.findViewById(R.id.brick_notif_btn_hint_layout);

        CheckBox cbInput = view.findViewById(R.id.brick_notif_btn_checkbox_input);
        cbInput.setChecked(hasInput);
        hintLayout.setVisibility(hasInput ? View.VISIBLE : View.GONE);

        cbInput.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hasInput = isChecked;
            hintLayout.setVisibility(hasInput ? View.VISIBLE : View.GONE);
        });

        CheckBox cbClose = view.findViewById(R.id.brick_notif_btn_checkbox_close);
        cbClose.setChecked(autoClose);
        cbClose.setOnCheckedChangeListener((buttonView, isChecked) -> autoClose = isChecked);

        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createNotificationAddButtonAction(
                sprite, sequence,
                getFormulaWithBrickField(BrickField.VALUE_1),
                getFormulaWithBrickField(BrickField.VALUE_2),
                getFormulaWithBrickField(BrickField.VALUE_3),
                getFormulaWithBrickField(BrickField.VALUE_4),
                getFormulaWithBrickField(BrickField.VALUE_5),
                behavior, hasInput, autoClose
        ));
    }
}
