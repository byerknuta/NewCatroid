package org.catrobat.catroid.raptor.postprocessing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.effects.ShaderVfxEffect;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;

public class CustomScreenShaderEffect extends ShaderVfxEffect implements ChainVfxEffect {

    private float time = 0f;

    public CustomScreenShaderEffect(String vertexCode, String fragmentCode) {
        super(new ShaderProgram(vertexCode, fragmentCode));
        if (!program.isCompiled()) {
            Gdx.app.error("CustomScreenShader", "Shader compile error:\n" + program.getLog());
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        time += delta;
    }

    @Override
    public void rebind() {
        super.rebind();
        program.begin();
        program.setUniformi("u_texture0", 0);
        program.end();
    }

    @Override
    public void render(VfxRenderContext context, VfxPingPongWrapper buffers) {
        VfxFrameBuffer src = buffers.getSrcBuffer();
        VfxFrameBuffer dst = buffers.getDstBuffer();

        program.begin();
        src.getTexture().bind(0);
        program.setUniformi("u_texture0", 0);
        program.setUniformf("u_time", time);
        program.end();

        renderShader(context, dst);
    }
}
