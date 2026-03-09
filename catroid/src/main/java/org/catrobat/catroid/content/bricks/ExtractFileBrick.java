package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class ExtractFileBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public ExtractFileBrick() {
        addAllowedBrickField(BrickField.TEXT_1, R.id.brick_extract_edit_apk);
        addAllowedBrickField(BrickField.TEXT_2, R.id.brick_extract_edit_inner);
        addAllowedBrickField(BrickField.TEXT_3, R.id.brick_extract_edit_dest);
    }

    public ExtractFileBrick(String apk, String inner, String dest) {
        this(new Formula(apk), new Formula(inner), new Formula(dest));
    }

    public ExtractFileBrick(Formula apk, Formula inner, Formula dest) {
        this();
        setFormulaWithBrickField(BrickField.TEXT_1, apk);
        setFormulaWithBrickField(BrickField.TEXT_2, inner);
        setFormulaWithBrickField(BrickField.TEXT_3, dest);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_extract_file;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createExtractFileAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.TEXT_1),
                        getFormulaWithBrickField(BrickField.TEXT_2),
                        getFormulaWithBrickField(BrickField.TEXT_3)));
    }
}