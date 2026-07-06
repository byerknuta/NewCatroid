package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class NotificationCancelBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public NotificationCancelBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_notif_cancel_id);
    }

    public NotificationCancelBrick(String id) {
        this(new Formula(id));
    }

    public NotificationCancelBrick(Formula id) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, id);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_notification_cancel;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createNotificationCancelAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1)));
    }
}
