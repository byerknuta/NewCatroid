package org.catrobat.catroid.content.bricks;

import android.content.Context;
import android.view.View;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.BroadcastWithParamsReceiverScript;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.UserVariable;

import java.util.List;

public class BroadcastWithParamsReceiverBrick extends UserVariableBrickWithFormula implements ScriptBrick {

    private static final long serialVersionUID = 1L;
    private BroadcastWithParamsReceiverScript script;

    public BroadcastWithParamsReceiverBrick() {
        this(new BroadcastWithParamsReceiverScript());
    }

    public BroadcastWithParamsReceiverBrick(BroadcastWithParamsReceiverScript script) {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_broadcast_with_params_receive_edit_signal);

        script.setScriptBrick(this);
        this.commentedOut = script.isCommentedOut();
        this.script = script;

        this.formulaMap = script.getFormulaMap();
    }

    public BroadcastWithParamsReceiverBrick(String signal, UserVariable variable) {
        this(new BroadcastWithParamsReceiverScript(new Formula(signal), variable));
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_broadcast_with_params_receive;
    }

    @Override
    protected int getSpinnerId() {
        return R.id.brick_broadcast_with_params_spinner_variable;
    }

    @Override
    public View getView(Context context) {
        this.userVariable = script.getUserVariable();
        return super.getView(context);
    }

    @Override
    public void onItemSelected(Integer spinnerId, UserVariable item) {
        super.onItemSelected(spinnerId, item);
        script.setUserVariable(item);
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public Brick clone() throws CloneNotSupportedException {
        BroadcastWithParamsReceiverBrick clone = (BroadcastWithParamsReceiverBrick) super.clone();
        clone.script = (BroadcastWithParamsReceiverScript) script.clone();
        clone.script.setScriptBrick(clone);
        clone.formulaMap = clone.script.getFormulaMap();
        return clone;
    }

    @Override
    public void addToFlatList(List<Brick> bricks) {
        super.addToFlatList(bricks);
        for (Brick brick : getScript().getBrickList()) {
            brick.addToFlatList(bricks);
        }
    }

    @Override
    public int getPositionInScript() {
        return -1;
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
