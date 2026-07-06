package org.catrobat.catroid.content;

import org.catrobat.catroid.content.bricks.ScriptBrick;
import org.catrobat.catroid.content.bricks.WhenFingerMovedOverSpriteBrick;
import org.catrobat.catroid.content.eventids.EventId;

public class WhenFingerMovedOverSpriteScript extends Script {
    private static final long serialVersionUID = 1L;

    @Override
    public ScriptBrick getScriptBrick() {
        if (scriptBrick == null) {
            scriptBrick = new WhenFingerMovedOverSpriteBrick(this);
        }
        return scriptBrick;
    }

    @Override
    public EventId createEventId(Sprite sprite) {
        return new EventId(EventId.FINGER_MOVED_OVER_SPRITE);
    }
}
