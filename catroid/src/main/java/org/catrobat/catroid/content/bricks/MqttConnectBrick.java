package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class MqttConnectBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public MqttConnectBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_mqtt_connect_id);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_mqtt_connect_host);
        addAllowedBrickField(BrickField.VALUE_3, R.id.brick_mqtt_connect_port);
    }

    public MqttConnectBrick(String clientId, String host, int port) {
        this(new Formula(clientId), new Formula(host), new Formula(port));
    }

    public MqttConnectBrick(Formula clientId, Formula host, Formula port) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, clientId);
        setFormulaWithBrickField(BrickField.VALUE_2, host);
        setFormulaWithBrickField(BrickField.VALUE_3, port);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_mqtt_connect;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createMqttConnectAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        getFormulaWithBrickField(BrickField.VALUE_3)));
    }
}
