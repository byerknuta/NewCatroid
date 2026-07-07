package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class StartBufferRecordingBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public StartBufferRecordingBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_start_buffer_recording_edit_buf);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_start_buffer_recording_edit_file);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_start_buffer_recording_edit_fps);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_start_buffer_recording_edit_bitrate);
    }

    public StartBufferRecordingBrick(String bufferName, String fileName, int fps, int bitrate) {
        this(new Formula(bufferName), new Formula(fileName), new Formula(fps), new Formula(bitrate));
    }

    public StartBufferRecordingBrick(Formula bufferName, Formula fileName, Formula fps, Formula bitrate) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, bufferName);
        setFormulaWithBrickField(BrickField.VALUE_2, fileName);
        setFormulaWithBrickField(BrickField.VALUE_3, fps);
        setFormulaWithBrickField(BrickField.VALUE_4, bitrate);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_start_buffer_recording;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createStartBufferRecordingAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.VALUE_4)));
    }
}
