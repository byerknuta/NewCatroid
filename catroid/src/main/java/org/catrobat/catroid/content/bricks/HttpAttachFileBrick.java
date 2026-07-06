package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class HttpAttachFileBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public HttpAttachFileBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_http_attach_file_id_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_http_attach_file_name_field);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_http_attach_file_parameter_field);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_http_attach_file_mime_field);
    }

    public HttpAttachFileBrick(String requestId, String fileName, String parameterName, String mimeType) {
        this(new Formula(requestId), new Formula(fileName), new Formula(parameterName), new Formula(mimeType));
    }

    public HttpAttachFileBrick(Formula requestId, Formula fileName, Formula parameterName, Formula mimeType) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, requestId);
        setFormulaWithBrickField(BrickField.VALUE_2, fileName);
        setFormulaWithBrickField(BrickField.VALUE_3, parameterName);
        setFormulaWithBrickField(BrickField.VALUE_4, mimeType);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_http_attach_file;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createHttpAttachFileAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.VALUE_4)));
    }
}
