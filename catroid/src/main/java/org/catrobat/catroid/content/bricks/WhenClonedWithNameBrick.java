package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.WhenClonedWithNameScript;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

import java.util.List;

public class WhenClonedWithNameBrick extends FormulaBrick implements ScriptBrick {
    private static final long serialVersionUID = 1L;

    private WhenClonedWithNameScript script;

    public WhenClonedWithNameBrick() {
        this(new WhenClonedWithNameScript());
    }

    public WhenClonedWithNameBrick(WhenClonedWithNameScript script) {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_when_cloned_with_name_edit_text);
        script.setScriptBrick(this);
        commentedOut = script.isCommentedOut();
        this.script = script;

        formulaMap = script.getFormulaMap();
    }

    @Override
    public Brick clone() throws CloneNotSupportedException {
        WhenClonedWithNameBrick clone = (WhenClonedWithNameBrick) super.clone();
        clone.script = (WhenClonedWithNameScript) script.clone();
        clone.script.setScriptBrick(clone);
        clone.formulaMap = clone.script.getFormulaMap();
        return clone;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_when_cloned_with_name;
    }

    public Formula getNameFormula() {
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
