package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class PlaySpritesheetBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public PlaySpritesheetBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_play_spritesheet_rows);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_play_spritesheet_cols);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_play_spritesheet_selected_row);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_play_spritesheet_frames_count);
        addAllowedBrickField(BrickField.VALUE_5, R.id.brick_play_spritesheet_speed_val);
    }

    public PlaySpritesheetBrick(Formula rows, Formula cols, Formula selectedRow, Formula framesCount, Formula speed) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, rows);
        setFormulaWithBrickField(BrickField.VALUE_2, cols);
        setFormulaWithBrickField(BrickField.VALUE_3, selectedRow);
        setFormulaWithBrickField(BrickField.VALUE_4, framesCount);
        setFormulaWithBrickField(BrickField.VALUE_5, speed);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_play_spritesheet;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createPlaySpritesheetAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.VALUE_4),
                        getFormulaWithBrickField(BrickField.VALUE_5)));
    }
}
