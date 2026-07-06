package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.WhenAppMinimizedScript;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;

public class WhenAppMinimizedBrick extends ScriptBrickBaseType {
    private static final long serialVersionUID = 1L;
    private WhenAppMinimizedScript script;

    public WhenAppMinimizedBrick() {
        this(new WhenAppMinimizedScript());
    }

    public WhenAppMinimizedBrick(WhenAppMinimizedScript script) {
        script.setScriptBrick(this);
        this.commentedOut = script.isCommentedOut();
        this.script = script;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_when_app_minimized;
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public Brick clone() throws CloneNotSupportedException {
        WhenAppMinimizedBrick clone = (WhenAppMinimizedBrick) super.clone();
        clone.script = (WhenAppMinimizedScript) script.clone();
        clone.script.setScriptBrick(clone);
        return clone;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
    }
}
