package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.WhenMqttMessageScript;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

import java.util.List;

public class WhenMqttMessageBrick extends FormulaBrick implements ScriptBrick {
    private static final long serialVersionUID = 1L;

    private WhenMqttMessageScript script;

    public WhenMqttMessageBrick() {
        this(new WhenMqttMessageScript());
    }

    public WhenMqttMessageBrick(WhenMqttMessageScript script) {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_when_mqtt_message_room_id);
        script.setScriptBrick(this);
        commentedOut = script.isCommentedOut();
        this.script = script;

        formulaMap = script.getFormulaMap();
    }

    @Override
    public Brick clone() throws CloneNotSupportedException {
        WhenMqttMessageBrick clone = (WhenMqttMessageBrick) super.clone();
        clone.script = (WhenMqttMessageScript) script.clone();
        clone.script.setScriptBrick(clone);
        clone.formulaMap = clone.script.getFormulaMap();
        return clone;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_when_mqtt_message;
    }

    public Formula getRoomIdFormula() {
        return getFormulaWithBrickField(BrickField.VALUE_1);
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public int getPositionInScript() {
        return -1;
    }

    @Override
    public void addToFlatList(List<Brick> bricks) {
        super.addToFlatList(bricks);
        for (Brick brick : getScript().getBrickList()) {
            brick.addToFlatList(bricks);
        }
    }

    @Override
    public List<Brick> getDragAndDropTargetList() {
        return getScript().getBrickList();
    }

    @Override
    public int getPositionInDragAndDropTargetList() {
        return -1;
    }

    @Override
    public void setCommentedOut(boolean commentedOut) {
        super.setCommentedOut(commentedOut);
        getScript().setCommentedOut(commentedOut);
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
    }
}
