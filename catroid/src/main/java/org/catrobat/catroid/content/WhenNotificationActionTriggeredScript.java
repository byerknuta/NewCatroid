package org.catrobat.catroid.content;

import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.ConcurrentFormulaHashMap;
import org.catrobat.catroid.content.bricks.ScriptBrick;
import org.catrobat.catroid.content.bricks.WhenNotificationActionTriggeredBrick;
import org.catrobat.catroid.content.eventids.EventId;
import org.catrobat.catroid.formulaeditor.Formula;

public class WhenNotificationActionTriggeredScript extends Script {
    private static final long serialVersionUID = 1L;

    private ConcurrentFormulaHashMap formulaMap = new ConcurrentFormulaHashMap();

    public WhenNotificationActionTriggeredScript() {
        formulaMap.putIfAbsent(Brick.BrickField.VALUE_1, new Formula(""));
    }

    public WhenNotificationActionTriggeredScript(Formula actionId) {
        this();
        formulaMap.replace(Brick.BrickField.VALUE_1, actionId);
    }

    public ConcurrentFormulaHashMap getFormulaMap() {
        return formulaMap;
    }

    @Override
    public Script clone() throws CloneNotSupportedException {
        WhenNotificationActionTriggeredScript clone = (WhenNotificationActionTriggeredScript) super.clone();
        clone.formulaMap = formulaMap.clone();
        return clone;
    }

    @Override
    public ScriptBrick getScriptBrick() {
        if (scriptBrick == null) {
            scriptBrick = new WhenNotificationActionTriggeredBrick(this);
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
        return new EventId(EventId.OTHER);
    }
}
