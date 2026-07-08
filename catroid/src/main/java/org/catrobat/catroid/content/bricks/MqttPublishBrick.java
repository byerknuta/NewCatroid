package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class MqttPublishBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public MqttPublishBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_mqtt_publish_client_id);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_mqtt_publish_room_id);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_mqtt_publish_salt);
        addAllowedBrickField(BrickField.VALUE_4, R.id.brick_mqtt_publish_message);
    }

    public MqttPublishBrick(String clientId, String roomId, String salt, String message) {
        this(new Formula(clientId), new Formula(roomId), new Formula(salt), new Formula(message));
    }

    public MqttPublishBrick(Formula clientId, Formula roomId, Formula salt, Formula message) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, clientId);
        setFormulaWithBrickField(BrickField.VALUE_2, roomId);
        setFormulaWithBrickField(BrickField.VALUE_3, salt);
        setFormulaWithBrickField(BrickField.VALUE_4, message);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_mqtt_publish;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createMqttPublishAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3),
                        getFormulaWithBrickField(BrickField.VALUE_4)));
    }
}
