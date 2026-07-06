package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.WhenFingerMovedOverSpriteScript;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;

public class WhenFingerMovedOverSpriteBrick extends ScriptBrickBaseType {
    private static final long serialVersionUID = 1L;
    private WhenFingerMovedOverSpriteScript script;

    public WhenFingerMovedOverSpriteBrick() {
        this(new WhenFingerMovedOverSpriteScript());
    }

    public WhenFingerMovedOverSpriteBrick(WhenFingerMovedOverSpriteScript script) {
        script.setScriptBrick(this);
        this.commentedOut = script.isCommentedOut();
        this.script = script;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_when_finger_moved_over_sprite;
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public Brick clone() throws CloneNotSupportedException {
        WhenFingerMovedOverSpriteBrick clone = (WhenFingerMovedOverSpriteBrick) super.clone();
        clone.script = (WhenFingerMovedOverSpriteScript) script.clone();
        clone.script.setScriptBrick(clone);
        return clone;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
    }
}
