package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.BeforeUpdateScript;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;

public class WhenBeforeUpdateBrick extends ScriptBrickBaseType {
    private static final long serialVersionUID = 1L;
    private BeforeUpdateScript script;

    public WhenBeforeUpdateBrick() {
        this(new BeforeUpdateScript());
    }

    public WhenBeforeUpdateBrick(BeforeUpdateScript script) {
        script.setScriptBrick(this);
        this.commentedOut = script.isCommentedOut();
        this.script = script;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_when_before_update;
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public Brick clone() throws CloneNotSupportedException {
        WhenBeforeUpdateBrick clone = (WhenBeforeUpdateBrick) super.clone();
        clone.script = (BeforeUpdateScript) script.clone();
        clone.script.setScriptBrick(clone);
        return clone;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
    }
}
