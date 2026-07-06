package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.WhenAppRestoredScript;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;

public class WhenAppRestoredBrick extends ScriptBrickBaseType {
    private static final long serialVersionUID = 1L;
    private WhenAppRestoredScript script;

    public WhenAppRestoredBrick() {
        this(new WhenAppRestoredScript());
    }

    public WhenAppRestoredBrick(WhenAppRestoredScript script) {
        script.setScriptBrick(this);
        this.commentedOut = script.isCommentedOut();
        this.script = script;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_when_app_restored;
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public Brick clone() throws CloneNotSupportedException {
        WhenAppRestoredBrick clone = (WhenAppRestoredBrick) super.clone();
        clone.script = (WhenAppRestoredScript) script.clone();
        clone.script.setScriptBrick(clone);
        return clone;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
    }
}
