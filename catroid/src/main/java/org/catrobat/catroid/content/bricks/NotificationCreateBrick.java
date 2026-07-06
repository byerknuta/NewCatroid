package org.catrobat.catroid.content.bricks;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.content.actions.NotificationCreateAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class NotificationCreateBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;
    private int importanceSelection = 1;
    private boolean isOngoing = false;

    public NotificationCreateBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_notif_create_id);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_notif_create_channel);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_notif_create_title);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_notif_create_text);
        addAllowedBrickField(BrickField.VALUE_5, R.id.brick_notif_create_icon);
    }

    public NotificationCreateBrick(String id, String channel, String title, String text, String iconFile) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(id));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(channel));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(title));
        setFormulaWithBrickField(BrickField.VALUE_4, new Formula(text));
        setFormulaWithBrickField(BrickField.VALUE_5, new Formula(iconFile));
    }

    @Override
    public int getViewResource() { return R.layout.brick_notification_create; }

    @Override
    public View getView(Context context) {
        super.getView(context);
        Spinner spinner = view.findViewById(R.id.brick_notif_create_importance);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.brick_notification_priorities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { importanceSelection = position; }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinner.setSelection(importanceSelection);

        CheckBox checkBox = view.findViewById(R.id.brick_notif_create_ongoing);
        checkBox.setChecked(isOngoing);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> isOngoing = isChecked);
        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createNotificationCreateAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.VALUE_4),
                        getFormulaWithBrickField(BrickField.VALUE_5),
                        importanceSelection,
                        isOngoing));
    }
}
