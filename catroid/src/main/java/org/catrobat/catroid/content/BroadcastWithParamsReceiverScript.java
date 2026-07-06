package org.catrobat.catroid.content;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.ConcurrentFormulaHashMap;
import org.catrobat.catroid.content.bricks.ScriptBrick;
import org.catrobat.catroid.content.bricks.BroadcastWithParamsReceiverBrick;
import org.catrobat.catroid.content.eventids.BroadcastEventId;
import org.catrobat.catroid.content.eventids.EventId;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.UserVariable;

public class BroadcastWithParamsReceiverScript extends Script {
    private static final long serialVersionUID = 1L;

    private ConcurrentFormulaHashMap formulaMap = new ConcurrentFormulaHashMap();
    private UserVariable userVariable;

    public BroadcastWithParamsReceiverScript() {
        formulaMap.putIfAbsent(Brick.BrickField.VALUE_1, new Formula("signal"));
    }

    public BroadcastWithParamsReceiverScript(Formula signalFormula, UserVariable userVariable) {
        formulaMap.putIfAbsent(Brick.BrickField.VALUE_1, signalFormula);
        this.userVariable = userVariable;
    }

    public ConcurrentFormulaHashMap getFormulaMap() {
        return formulaMap;
    }

    @Override
    public Script clone() throws CloneNotSupportedException {
        BroadcastWithParamsReceiverScript clone = (BroadcastWithParamsReceiverScript) super.clone();
        clone.formulaMap = formulaMap.clone();
        return clone;
    }

    @Override
    public ScriptBrick getScriptBrick() {
        if (scriptBrick == null) {
            scriptBrick = new BroadcastWithParamsReceiverBrick(this);
        }
        return scriptBrick;
    }

    public UserVariable getUserVariable() {
        return userVariable;
    }

    public void setUserVariable(UserVariable userVariable) {
        this.userVariable = userVariable;
    }

    @Override
    public EventId createEventId(Sprite sprite) {
        String signal = "";
        try {
            Formula f = formulaMap.get(Brick.BrickField.VALUE_1);
            if (f != null) {
                signal = f.interpretString(new Scope(ProjectManager.getInstance().getCurrentProject(), sprite, null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BroadcastEventId(signal);
    }

    @Override
    public void run(Sprite sprite, ScriptSequenceAction sequence) {
        if (commentedOut) {
            return;
        }

        String signalName = "";
        try {
            Formula f = formulaMap.get(Brick.BrickField.VALUE_1);
            if (f != null) {
                signalName = f.interpretString(new Scope(ProjectManager.getInstance().getCurrentProject(), sprite, null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        sequence.addAction(sprite.getActionFactory()
                .createSaveBroadcastParamsAction(sprite, sequence, signalName, getUserVariable()));

        super.run(sprite, sequence);
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
}
