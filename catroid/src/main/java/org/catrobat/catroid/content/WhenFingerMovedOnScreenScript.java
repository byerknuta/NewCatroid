package org.catrobat.catroid.content;

import org.catrobat.catroid.content.bricks.ScriptBrick;
import org.catrobat.catroid.content.bricks.WhenFingerMovedOnScreenBrick;
import org.catrobat.catroid.content.eventids.EventId;

public class WhenFingerMovedOnScreenScript extends Script {
    private static final long serialVersionUID = 1L;

    @Override
    public ScriptBrick getScriptBrick() {
        if (scriptBrick == null) {
            scriptBrick = new WhenFingerMovedOnScreenBrick(this);
        }
        return scriptBrick;
    }

    @Override
    public EventId createEventId(Sprite sprite) {
        return new EventId(EventId.FINGER_MOVED_ON_SCREEN);
    }
}
