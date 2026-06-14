package org.catrobat.catroid.raptor.postprocessing;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import java.util.HashMap;
import java.util.Map;

public class CustomShaderAttribute extends Attribute {
    public static final String Alias = "CustomShader";
    public static final long Type = register(Alias);

    public ShaderProgram program;
    public final Map<String, Object> uniforms = new HashMap<>();

    public CustomShaderAttribute(ShaderProgram program) {
        super(Type);
        this.program = program;
    }

    @Override
    public Attribute copy() {
        CustomShaderAttribute copy = new CustomShaderAttribute(program);
        copy.uniforms.putAll(this.uniforms);
        return copy;
    }

    @Override
    public int compareTo(Attribute other) {
        if (type != other.type) return (int) (type - other.type);
        return 0;
    }
}
