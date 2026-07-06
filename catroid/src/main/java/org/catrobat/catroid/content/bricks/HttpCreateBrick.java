package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class HttpCreateBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public HttpCreateBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_http_create_id_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_http_create_method_field);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_http_create_url_field);
    }

    public HttpCreateBrick(String requestId, String method, String url) {
        this(new Formula(requestId), new Formula(method), new Formula(url));
    }

    public HttpCreateBrick(Formula requestId, Formula method, Formula url) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, requestId);
        setFormulaWithBrickField(BrickField.VALUE_2, method);
        setFormulaWithBrickField(BrickField.VALUE_3, url);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_http_create;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createHttpCreateAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3)));
    }
}
