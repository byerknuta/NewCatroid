package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class HttpSaveFileBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public HttpSaveFileBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_http_save_file_id_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_http_save_file_dest_field);
    }

    public HttpSaveFileBrick(String requestId, String destinationFileName) {
        this(new Formula(requestId), new Formula(destinationFileName));
    }

    public HttpSaveFileBrick(Formula requestId, Formula destinationFileName) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, requestId);
        setFormulaWithBrickField(BrickField.VALUE_2, destinationFileName);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_http_save_file;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createHttpSaveFileAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2)));
    }
}
