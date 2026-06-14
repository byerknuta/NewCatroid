package org.catrobat.catroid.raptor.postprocessing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import java.util.Map;

public class CustomObjectShader extends DefaultShader {
    private final CustomShaderAttribute attribute;

    public CustomObjectShader(Renderable renderable, CustomShaderAttribute attribute) {
        super(renderable, new DefaultShader.Config(), attribute.program);
        this.attribute = attribute;
    }

    @Override
    public void render(Renderable renderable) {
        int uTimeLoc = program.getUniformLocation("u_time");
        if (uTimeLoc != -1) {
            float time = (float) ((System.currentTimeMillis() % 100000) / 1000.0);
            program.setUniformf(uTimeLoc, time);
        }

        int uCamLoc = program.getUniformLocation("u_customCameraPosition");
        if (uCamLoc != -1 && camera != null) {
            program.setUniformf(uCamLoc, camera.position.x, camera.position.y, camera.position.z);
        }

        for (Map.Entry<String, Object> entry : attribute.uniforms.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            int loc = program.getUniformLocation(name);
            if (loc != -1) {
                if (value instanceof Float) {
                    program.setUniformf(loc, (Float) value);
                } else if (value instanceof Vector2) {
                    Vector2 v2 = (Vector2) value;
                    program.setUniformf(loc, v2.x, v2.y);
                } else if (value instanceof Vector3) {
                    Vector3 v3 = (Vector3) value;
                    program.setUniformf(loc, v3.x, v3.y, v3.z);
                }
            }
        }

        super.render(renderable);
    }
}
