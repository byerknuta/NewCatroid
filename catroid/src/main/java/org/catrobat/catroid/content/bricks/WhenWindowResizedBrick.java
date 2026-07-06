package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.WhenWindowResizedScript;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;

public class WhenWindowResizedBrick extends ScriptBrickBaseType {
    private static final long serialVersionUID = 1L;
    private WhenWindowResizedScript script;

    public WhenWindowResizedBrick() {
        this(new WhenWindowResizedScript());
    }

    public WhenWindowResizedBrick(WhenWindowResizedScript script) {
        script.setScriptBrick(this);
        this.commentedOut = script.isCommentedOut();
        this.script = script;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_when_window_resized;
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public Brick clone() throws CloneNotSupportedException {
        WhenWindowResizedBrick clone = (WhenWindowResizedBrick) super.clone();
        clone.script = (WhenWindowResizedScript) script.clone();
        clone.script.setScriptBrick(clone);
        return clone;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
    }
}
