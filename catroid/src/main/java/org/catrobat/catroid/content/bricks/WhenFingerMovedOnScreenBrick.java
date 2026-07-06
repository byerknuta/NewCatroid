package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.WhenFingerMovedOnScreenScript;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;

public class WhenFingerMovedOnScreenBrick extends ScriptBrickBaseType {
    private static final long serialVersionUID = 1L;
    private WhenFingerMovedOnScreenScript script;

    public WhenFingerMovedOnScreenBrick() {
        this(new WhenFingerMovedOnScreenScript());
    }

    public WhenFingerMovedOnScreenBrick(WhenFingerMovedOnScreenScript script) {
        script.setScriptBrick(this);
        this.commentedOut = script.isCommentedOut();
        this.script = script;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_when_finger_moved_on_screen;
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public Brick clone() throws CloneNotSupportedException {
        WhenFingerMovedOnScreenBrick clone = (WhenFingerMovedOnScreenBrick) super.clone();
        clone.script = (WhenFingerMovedOnScreenScript) script.clone();
        clone.script.setScriptBrick(clone);
        return clone;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
    }
}
