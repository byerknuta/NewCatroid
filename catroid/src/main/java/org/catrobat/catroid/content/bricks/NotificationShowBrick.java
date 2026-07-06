package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.content.actions.NotificationShowAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class NotificationShowBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public NotificationShowBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_notif_show_id);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_notif_show_delay);
    }

    public NotificationShowBrick(String id, double delaySeconds) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, new Formula(id));
        setFormulaWithBrickField(BrickField.VALUE_2, new Formula(delaySeconds));
    }

    @Override
    public int getViewResource() { return R.layout.brick_notification_show; }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createNotificationShowAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2)));
    }
}
