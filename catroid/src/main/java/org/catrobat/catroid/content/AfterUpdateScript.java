package org.catrobat.catroid.content;

import org.catrobat.catroid.content.bricks.ScriptBrick;
import org.catrobat.catroid.content.bricks.WhenAfterUpdateBrick;
import org.catrobat.catroid.content.eventids.EventId;

public class AfterUpdateScript extends Script {
    private static final long serialVersionUID = 1L;

    @Override
    public ScriptBrick getScriptBrick() {
        if (scriptBrick == null) {
            scriptBrick = new WhenAfterUpdateBrick(this);
        }
        return scriptBrick;
    }

    @Override
    public EventId createEventId(Sprite sprite) {
        return new EventId(EventId.AFTER_UPDATE);
    }
}
