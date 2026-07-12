package org.catrobat.catroid.content.actions;

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import org.catrobat.catroid.content.Look;
import org.catrobat.catroid.content.Sprite;

public class SetRotationStyleAction extends TemporalAction {

    private Sprite sprite;
    @Look.RotationStyle
    private int mode;

    @Override
    protected void update(float percent) {
        sprite.look.setRotationMode(mode);
        sprite.look.setMotionDirectionInUserInterfaceDimensionUnit(
                sprite.look.getMotionDirectionInUserInterfaceDimensionUnit()
        );
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    public void setRotationStyle(@Look.RotationStyle int mode) {
        this.mode = mode;
    }
}
