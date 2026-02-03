package org.catrobat.catroid.content.bricks;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class KeyframeAnimationBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;
    private int actionSelection = 0; // 0: Play, 1: Stop, 2: Set Time

    public KeyframeAnimationBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_keyframe_anim_id);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_keyframe_anim_time_value);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_keyframe_animation;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);
        Spinner actionSpinner = view.findViewById(R.id.brick_keyframe_anim_spinner);
        LinearLayout timeLayout = view.findViewById(R.id.layout_keyframe_time_input);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.keyframe_actions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionSpinner.setAdapter(adapter);

        actionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actionSelection = position;
                // Показываем поле ввода времени только для "Jump to Time" (индекс 2)
                timeLayout.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        actionSpinner.setSelection(actionSelection);
        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createKeyframeAnimationAction(
                sprite, sequence,
                getFormulaWithBrickField(BrickField.VALUE_1),
                actionSelection,
                getFormulaWithBrickField(BrickField.VALUE_2)
        ));
    }
}