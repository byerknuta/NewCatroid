package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class MqttDisconnectBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public MqttDisconnectBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_mqtt_disconnect_client_id);
    }

    public MqttDisconnectBrick(String clientId) {
        this(new Formula(clientId));
    }

    public MqttDisconnectBrick(Formula clientId) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, clientId);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_mqtt_disconnect;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createMqttDisconnectAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1)));
    }
}
