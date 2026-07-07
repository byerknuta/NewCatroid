package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.UserVariable;

public class JavaCompileBrick extends UserVariableBrickWithFormula {
    private static final long serialVersionUID = 1L;

    public JavaCompileBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_java_compile_src_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_java_compile_dest_field);
    }

    public JavaCompileBrick(Formula srcFolder, Formula destDexFile, UserVariable errorVariable) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, srcFolder);
        setFormulaWithBrickField(BrickField.VALUE_2, destDexFile);
        this.userVariable = errorVariable;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_java_compile;
    }

    @Override
    protected int getSpinnerId() {
        return R.id.java_compile_error_spinner;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createCompileJavaToDexAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        userVariable));
    }
}
