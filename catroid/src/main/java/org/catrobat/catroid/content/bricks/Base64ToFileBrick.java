package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class Base64ToFileBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public Base64ToFileBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_base64_to_file_source);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_base64_to_file_destination);
    }

    public Base64ToFileBrick(String base64String, String destinationFileName) {
        this(new Formula(base64String), new Formula(destinationFileName));
    }

    public Base64ToFileBrick(Formula base64String, Formula destinationFileName) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, base64String);
        setFormulaWithBrickField(BrickField.VALUE_2, destinationFileName);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_base64_to_file;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createBase64ToFileAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2)));
    }
}
