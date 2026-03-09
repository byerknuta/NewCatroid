package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SignApkBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public SignApkBrick() {
        addAllowedBrickField(BrickField.TEXT_1, R.id.brick_sign_apk_edit_in);
        addAllowedBrickField(BrickField.TEXT_2, R.id.brick_sign_apk_edit_out);
        addAllowedBrickField(BrickField.TEXT_3, R.id.brick_sign_apk_edit_key);
        addAllowedBrickField(BrickField.TEXT_4, R.id.brick_sign_apk_edit_pass);
        addAllowedBrickField(BrickField.TEXT_5, R.id.brick_sign_apk_edit_alias);
    }

    public SignApkBrick(String input, String output, String key, String pass, String alias) {
        this(new Formula(input), new Formula(output), new Formula(key), new Formula(pass), new Formula(alias));
    }

    public SignApkBrick(Formula input, Formula output, Formula key, Formula pass, Formula alias) {
        this();
        setFormulaWithBrickField(BrickField.TEXT_1, input);
        setFormulaWithBrickField(BrickField.TEXT_2, output);
        setFormulaWithBrickField(BrickField.TEXT_3, key);
        setFormulaWithBrickField(BrickField.TEXT_4, pass);
        setFormulaWithBrickField(BrickField.TEXT_5, alias);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_sign_apk;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createSignApkAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.TEXT_1),
                        getFormulaWithBrickField(BrickField.TEXT_2),
                        getFormulaWithBrickField(BrickField.TEXT_3),
                        getFormulaWithBrickField(BrickField.TEXT_4),
                        getFormulaWithBrickField(BrickField.TEXT_5)));
    }
}