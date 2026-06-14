package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class RemoveObjectsByPrefixBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public RemoveObjectsByPrefixBrick() {
        addAllowedBrickField(BrickField.VALUE, R.id.brick_remove_prefix_val);
    }

    public RemoveObjectsByPrefixBrick(String prefix) {
        this(new Formula(prefix));
    }

    public RemoveObjectsByPrefixBrick(Formula prefix) {
        this();
        setFormulaWithBrickField(BrickField.VALUE, prefix);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_remove_objects_by_prefix;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createRemoveObjectsByPrefixAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE)));
    }
}
