package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.WhenSpriteReleasedScript;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;

public class WhenSpriteReleasedBrick extends ScriptBrickBaseType {
    private static final long serialVersionUID = 1L;
    private WhenSpriteReleasedScript script;

    public WhenSpriteReleasedBrick() {
        this(new WhenSpriteReleasedScript());
    }

    public WhenSpriteReleasedBrick(WhenSpriteReleasedScript script) {
        script.setScriptBrick(this);
        this.commentedOut = script.isCommentedOut();
        this.script = script;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_when_sprite_released;
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public Brick clone() throws CloneNotSupportedException {
        WhenSpriteReleasedBrick clone = (WhenSpriteReleasedBrick) super.clone();
        clone.script = (WhenSpriteReleasedScript) script.clone();
        clone.script.setScriptBrick(clone);
        return clone;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
    }
}
