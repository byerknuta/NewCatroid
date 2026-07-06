package org.catrobat.catroid.content;

import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.ConcurrentFormulaHashMap;
import org.catrobat.catroid.content.bricks.ScriptBrick;
import org.catrobat.catroid.content.bricks.WhenNotificationClickedBrick;
import org.catrobat.catroid.content.eventids.EventId;
import org.catrobat.catroid.formulaeditor.Formula;

public class WhenNotificationClickedScript extends Script {

    private static final long serialVersionUID = 1L;

    private ConcurrentFormulaHashMap formulaMap = new ConcurrentFormulaHashMap();

    public WhenNotificationClickedScript() {
        formulaMap.putIfAbsent(Brick.BrickField.VALUE_1, new Formula(""));
    }

    public WhenNotificationClickedScript(Formula notificationId) {
        this();
        formulaMap.replace(Brick.BrickField.VALUE_1, notificationId);
    }

    public ConcurrentFormulaHashMap getFormulaMap() {
        return formulaMap;
    }

    @Override
    public Script clone() throws CloneNotSupportedException {
        WhenNotificationClickedScript clone = (WhenNotificationClickedScript) super.clone();
        clone.formulaMap = formulaMap.clone();
        return clone;
    }

    @Override
    public ScriptBrick getScriptBrick() {
        if (scriptBrick == null) {
            scriptBrick = new WhenNotificationClickedBrick(this);
        }
        return scriptBrick;
    }

    @Override
    public void addRequiredResources(final Brick.ResourcesSet requiredResourcesSet) {
        for (Formula formula : formulaMap.values()) {
            formula.addRequiredResources(requiredResourcesSet);
        }
        for (Brick brick : brickList) {
            brick.addRequiredResources(requiredResourcesSet);
        }
    }

    @Override
    public EventId createEventId(Sprite sprite) {
        return new EventId(EventId.NOTIFICATION_CLICKED);
    }
}
