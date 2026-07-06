package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.WhenNotificationClickedScript;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

import java.util.List;

public class WhenNotificationClickedBrick extends FormulaBrick implements ScriptBrick {

    private static final long serialVersionUID = 1L;
    private WhenNotificationClickedScript script;

    public WhenNotificationClickedBrick() {
        this(new WhenNotificationClickedScript());
    }

    public WhenNotificationClickedBrick(WhenNotificationClickedScript script) {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_when_notification_clicked_id);
        script.setScriptBrick(this);
        commentedOut = script.isCommentedOut();
        this.script = script;
        formulaMap = script.getFormulaMap();
    }

    @Override
    public Brick clone() throws CloneNotSupportedException {
        WhenNotificationClickedBrick clone = (WhenNotificationClickedBrick) super.clone();
        clone.script = (WhenNotificationClickedScript) script.clone();
        clone.script.setScriptBrick(clone);
        clone.formulaMap = clone.script.getFormulaMap();
        return clone;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_when_notification_clicked;
    }

    public Formula getNotificationIdFormula() {
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
