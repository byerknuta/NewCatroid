package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class GenerateKeyBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public GenerateKeyBrick() {
        addAllowedBrickField(BrickField.TEXT_1, R.id.brick_gen_key_edit_filename);
        addAllowedBrickField(BrickField.TEXT_2, R.id.brick_gen_key_edit_pass);
        addAllowedBrickField(BrickField.TEXT_3, R.id.brick_gen_key_edit_alias);
        addAllowedBrickField(BrickField.TEXT_4, R.id.brick_gen_key_edit_name);
    }

    public GenerateKeyBrick(String filename, String pass, String alias, String name) {
        this(new Formula(filename), new Formula(pass), new Formula(alias), new Formula(name));
    }

    public GenerateKeyBrick(Formula filename, Formula pass, Formula alias, Formula name) {
        this();
        setFormulaWithBrickField(BrickField.TEXT_1, filename);
        setFormulaWithBrickField(BrickField.TEXT_2, pass);
        setFormulaWithBrickField(BrickField.TEXT_3, alias);
        setFormulaWithBrickField(BrickField.TEXT_4, name);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_generate_key;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createGenerateKeyAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.TEXT_1),
                        getFormulaWithBrickField(BrickField.TEXT_2),
                        getFormulaWithBrickField(BrickField.TEXT_3),
                        getFormulaWithBrickField(BrickField.TEXT_4)));
    }
}