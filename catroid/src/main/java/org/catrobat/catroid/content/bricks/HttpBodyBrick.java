package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class HttpBodyBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public HttpBodyBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_http_body_id_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_http_body_text_field);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_http_body_content_type_field);
    }

    public HttpBodyBrick(String requestId, String bodyText, String contentType) {
        this(new Formula(requestId), new Formula(bodyText), new Formula(contentType));
    }

    public HttpBodyBrick(Formula requestId, Formula bodyText, Formula contentType) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, requestId);
        setFormulaWithBrickField(BrickField.VALUE_2, bodyText);
        setFormulaWithBrickField(BrickField.VALUE_3, contentType);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_http_body;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createHttpBodyAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3)));
    }
}
