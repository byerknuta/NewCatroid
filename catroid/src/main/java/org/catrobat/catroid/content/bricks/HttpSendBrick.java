package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class HttpSendBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public HttpSendBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_http_send_id_field);
    }

    public HttpSendBrick(String requestId) {
        this(new Formula(requestId));
    }

    public HttpSendBrick(Formula requestId) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, requestId);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_http_send;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createHttpSendAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1)));
    }
}
