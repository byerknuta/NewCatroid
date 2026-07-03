package org.catrobat.catroid.content;

import org.catrobat.catroid.content.bricks.ScriptBrick;
import org.catrobat.catroid.content.bricks.WhenBeforeUpdateBrick;
import org.catrobat.catroid.content.eventids.EventId;

public class BeforeUpdateScript extends Script {
    private static final long serialVersionUID = 1L;

    @Override
    public ScriptBrick getScriptBrick() {
        if (scriptBrick == null) {
            scriptBrick = new WhenBeforeUpdateBrick(this);
        }
        return scriptBrick;
    }

    @Override
    public EventId createEventId(Sprite sprite) {
        return new EventId(EventId.BEFORE_UPDATE);
    }
}
