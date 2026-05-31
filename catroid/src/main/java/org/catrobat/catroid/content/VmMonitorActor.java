package org.catrobat.catroid.content;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class VmMonitorActor extends Actor {
    private Texture vmTexture;
    private ShaderProgram shader;

    public VmMonitorActor(ShaderProgram shader) {
        this.shader = shader;
    }

    public void setTexture(Texture texture) {
        this.vmTexture = texture;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (vmTexture == null) return;

        ShaderProgram previousShader = batch.getShader();
        float oldColor = batch.getPackedColor();
        boolean wasBlending = batch.isBlendingEnabled();

        batch.setShader(shader);
        batch.setColor(1, 1, 1, parentAlpha);

        if (wasBlending) {
            batch.disableBlending();
        }

        batch.draw(vmTexture, getX(), getY(), getWidth(), getHeight());

        if (wasBlending) {
            batch.enableBlending();
        }
        batch.setPackedColor(oldColor);
        batch.setShader(previousShader);
    }
}
