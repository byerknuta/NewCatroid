package org.catrobat.catroid.content;

import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.ConcurrentFormulaHashMap;
import org.catrobat.catroid.content.bricks.ScriptBrick;
import org.catrobat.catroid.content.bricks.WhenClonedWithNameBrick;
import org.catrobat.catroid.content.eventids.EventId;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;

public class WhenClonedWithNameScript extends Script {
    private static final long serialVersionUID = 1L;

    private ConcurrentFormulaHashMap formulaMap = new ConcurrentFormulaHashMap();

    public WhenClonedWithNameScript() {
        formulaMap.putIfAbsent(Brick.BrickField.VALUE_1, new Formula("clone_name"));
    }

    public WhenClonedWithNameScript(Formula nameFormula) {
        this();
        formulaMap.replace(Brick.BrickField.VALUE_1, nameFormula);
    }

    public ConcurrentFormulaHashMap getFormulaMap() {
        return formulaMap;
    }

    @Override
    public Script clone() throws CloneNotSupportedException {
        WhenClonedWithNameScript clone = (WhenClonedWithNameScript) super.clone();
        clone.formulaMap = formulaMap.clone();
        return clone;
    }

    @Override
    public ScriptBrick getScriptBrick() {
        if (scriptBrick == null) {
            scriptBrick = new WhenClonedWithNameBrick(this);
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
        return new EventId(EventId.START_AS_CLONE);
    }

    @Override
    public void run(Sprite sprite, ScriptSequenceAction sequence) {
        if (commentedOut) {
            return;
        }

        WhenClonedWithNameBrick brick = (WhenClonedWithNameBrick) getScriptBrick();
        Formula nameFormula = brick.getNameFormula();
        if (nameFormula != null) {
            try {
                org.catrobat.catroid.content.Scope scope = new org.catrobat.catroid.content.Scope(
                        org.catrobat.catroid.ProjectManager.getInstance().getCurrentProject(),
                        sprite,
                        null
                );
                String expectedName = nameFormula.interpretString(scope);

                if (!sprite.getName().equals(expectedName)) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        super.run(sprite, sequence);
    }
}
