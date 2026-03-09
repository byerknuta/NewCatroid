package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class DeleteFromApkBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public DeleteFromApkBrick() {
        addAllowedBrickField(BrickField.TEXT_1, R.id.brick_del_apk_edit_path);
        addAllowedBrickField(BrickField.TEXT_2, R.id.brick_del_apk_edit_pattern);
    }

    public DeleteFromApkBrick(String apk, String pattern) {
        this(new Formula(apk), new Formula(pattern));
    }

    public DeleteFromApkBrick(Formula apk, Formula pattern) {
        this();
        setFormulaWithBrickField(BrickField.TEXT_1, apk);
        setFormulaWithBrickField(BrickField.TEXT_2, pattern);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_delete_from_apk;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createDeleteFromApkAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.TEXT_1),
                        getFormulaWithBrickField(BrickField.TEXT_2)));
    }
}