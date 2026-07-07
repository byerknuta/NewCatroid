package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class JavaLoadAndRunBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public JavaLoadAndRunBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_java_load_path_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_java_load_class_field);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_java_load_method_field);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_java_load_arg_field);
    }

    public JavaLoadAndRunBrick(String dexPath, String className, String methodName, String methodArg) {
        this(new Formula(dexPath), new Formula(className), new Formula(methodName), new Formula(methodArg));
    }

    public JavaLoadAndRunBrick(Formula dexPath, Formula className, Formula methodName, Formula methodArg) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, dexPath);
        setFormulaWithBrickField(BrickField.VALUE_2, className);
        setFormulaWithBrickField(BrickField.VALUE_3, methodName);
        setFormulaWithBrickField(BrickField.VALUE_4, methodArg);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_java_load_and_run;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createLoadAndRunDexAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.VALUE_4)));
    }
}
