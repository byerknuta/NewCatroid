package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class MqttJoinRoomBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public MqttJoinRoomBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_mqtt_join_room_client_id);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_mqtt_join_room_id);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_mqtt_join_room_salt);
    }

    public MqttJoinRoomBrick(String clientId, String roomId, String salt) {
        this(new Formula(clientId), new Formula(roomId), new Formula(salt));
    }

    public MqttJoinRoomBrick(Formula clientId, Formula roomId, Formula salt) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, clientId);
        setFormulaWithBrickField(BrickField.VALUE_2, roomId);
        setFormulaWithBrickField(BrickField.VALUE_3, salt);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_mqtt_join_room;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createMqttJoinRoomAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3)));
    }
}
