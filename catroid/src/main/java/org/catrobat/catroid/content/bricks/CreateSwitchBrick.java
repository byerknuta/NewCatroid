package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class CreateSwitchBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public CreateSwitchBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_id_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_text_field);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_state_field);
        addAllowedBrickField(BrickField.X_POSITION, R.id.brick_x_field);
        addAllowedBrickField(BrickField.Y_POSITION, R.id.brick_y_field);
        addAllowedBrickField(BrickField.WIDTH, R.id.brick_w_field);
        addAllowedBrickField(BrickField.HEIGHT, R.id.brick_h_field);
    }

    public CreateSwitchBrick(String viewId, String text, Integer defaultState, int x, int y, int w, int h) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(viewId));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(text));
        setFormulaWithBrickField(BrickField.VALUE_3, new Formula(defaultState));
        setFormulaWithBrickField(BrickField.X_POSITION, new Formula(x));
        setFormulaWithBrickField(BrickField.Y_POSITION, new Formula(y));
        setFormulaWithBrickField(BrickField.WIDTH, new Formula(w));
        setFormulaWithBrickField(BrickField.HEIGHT, new Formula(h));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_create_switch;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createCreateSwitchAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.X_POSITION),
                        getFormulaWithBrickField(BrickField.Y_POSITION),
                        getFormulaWithBrickField(BrickField.WIDTH),
                        getFormulaWithBrickField(BrickField.HEIGHT)));
    }
}
