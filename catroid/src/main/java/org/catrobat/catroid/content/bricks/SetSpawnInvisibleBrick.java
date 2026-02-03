package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetSpawnInvisibleBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public SetSpawnInvisibleBrick() {
        addAllowedBrickField(BrickField.STRING, R.id.brick_spawn_invisible_edit_text);
    }

    public SetSpawnInvisibleBrick(String objectId) {
        this();
        setFormulaWithBrickField(BrickField.STRING, new Formula(objectId));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_spawn_invisible;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        var action = (org.catrobat.catroid.content.actions.SetSpawnInvisibleAction)
                sprite.getActionFactory().createSetSpawnInvisibleAction(
                        sprite, sequence, getFormulaWithBrickField(BrickField.STRING));
        sequence.addAction(action);
    }
}