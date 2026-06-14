package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetScreenShaderBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public SetScreenShaderBrick() {
        addAllowedBrickField(BrickField.VERTEX, R.id.brick_screen_shader_vertex);
        addAllowedBrickField(BrickField.FRAGMENT, R.id.brick_screen_shader_fragment);
    }

    public SetScreenShaderBrick(String vertex, String fragment) {
        this(new Formula(vertex), new Formula(fragment));
    }

    public SetScreenShaderBrick(Formula vertex, Formula fragment) {
        this();
        setFormulaWithBrickField(BrickField.VERTEX, vertex);
        setFormulaWithBrickField(BrickField.FRAGMENT, fragment);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_screen_shader;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createSetScreenShaderAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VERTEX),
                        getFormulaWithBrickField(BrickField.FRAGMENT)));
    }
}
