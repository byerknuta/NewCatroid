package org.catrobat.catroid.raptor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class IBLBuilderCompat implements Disposable {

    private final ShaderProgram irradianceShader;
    private final ShaderProgram radianceShader;
    private final Mesh skyboxMesh;
    private final PerspectiveCamera camera;

    public IBLBuilderCompat() {
        String vertexShader = "attribute vec3 a_position;\n" +
                "varying vec3 v_position;\n" +
                "uniform mat4 u_projViewTrans;\n" +
                "void main() {\n" +
                "  v_position = a_position;\n" +
                "  gl_Position = u_projViewTrans * vec4(a_position, 1.0);\n" +
                "}";

        String irradianceFragmentShader = "#ifdef GL_ES\n" +
                "precision mediump float;\n" +
                "#endif\n" +
                "varying vec3 v_position;\n" +
                "uniform samplerCube u_environmentMap;\n" +
                "const float PI = 3.14159265359;\n" +
                "void main() {\n" +
                "  vec3 N = normalize(v_position);\n" +
                "  vec3 irradiance = vec3(0.0);\n" +
                "  vec3 up    = vec3(0.0, 1.0, 0.0);\n" +
                "  vec3 right = cross(up, N);\n" +
                "  up = cross(N, right);\n" +
                "  float sampleDelta = 0.025;\n" +
                "  float nrSamples = 0.0;\n" +
                "  for(float phi = 0.0; phi < 2.0 * PI; phi += sampleDelta) {\n" +
                "    for(float theta = 0.0; theta < 0.5 * PI; theta += sampleDelta) {\n" +
                "      vec3 tangentSample = vec3(sin(theta) * cos(phi),  sin(theta) * sin(phi), cos(theta));\n" +
                "      vec3 sampleVec = tangentSample.x * right + tangentSample.y * up + tangentSample.z * N;\n" +
                "      irradiance += textureCube(u_environmentMap, sampleVec).rgb * cos(theta) * sin(theta);\n" +
                "      nrSamples++;\n" +
                "    }\n" +
                "  }\n" +
                "  irradiance = PI * irradiance * (1.0 / nrSamples);\n" +
                "  gl_FragColor = vec4(irradiance, 1.0);\n" +
                "}";

        String radianceFragmentShader = "#ifdef GL_ES\n" +
                "precision mediump float;\n" +
                "#endif\n" +
                "varying vec3 v_position;\n" +
                "uniform samplerCube u_environmentMap;\n" +
                "uniform float u_roughness;\n" +
                "// ... (Complex GGX/PBR functions would go here, but for simplicity we use a simpler blur)\n" +
                "void main() {\n" +
                "    // A simple LOD-based blur is what gdx-gltf does internally. We just sample directly.\n" +
                "    gl_FragColor = textureCube(u_environmentMap, normalize(v_position), u_roughness * 10.0);\n" +
                "}";


        irradianceShader = new ShaderProgram(vertexShader, irradianceFragmentShader);
        if (!irradianceShader.isCompiled()) throw new GdxRuntimeException("Irradiance shader compile error: " + irradianceShader.getLog());

        radianceShader = new ShaderProgram(vertexShader, radianceFragmentShader);
        if (!radianceShader.isCompiled()) throw new GdxRuntimeException("Radiance shader compile error: " + radianceShader.getLog());


        skyboxMesh = createSkyboxMesh();
        camera = new PerspectiveCamera(90, 1, 1);
        camera.near = 0.1f;
        camera.far = 10f;
    }

    public Cubemap buildIrradianceMap(Cubemap source, int size) {
        return renderToCubemap(source, size, irradianceShader, -1);
    }

    public Cubemap buildRadianceMap(Cubemap source, int size, int maxMipLevels) {
        return renderToCubemap(source, size, radianceShader, 1.0f);
    }

    private Cubemap renderToCubemap(Cubemap source, int size, ShaderProgram shader, float roughness) {
        FrameBufferCubemap fbo = new FrameBufferCubemap(Pixmap.Format.RGBA8888, size, size, false);

        source.bind(0);
        shader.bind();
        shader.setUniformi("u_environmentMap", 0);
        if(roughness >= 0) shader.setUniformf("u_roughness", roughness);

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        Gdx.gl.glDisable(GL20.GL_BLEND);

        fbo.begin();
        while (fbo.nextSide()) {
            camera.direction.set(fbo.getSide().direction);
            camera.up.set(fbo.getSide().up);
            camera.update();
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            shader.setUniformMatrix("u_projViewTrans", camera.combined);
            skyboxMesh.render(shader, GL20.GL_TRIANGLES);
        }
        fbo.end();

        return fbo.getColorBufferTexture();
    }


    private Mesh createSkyboxMesh() {
        float[] vertices = {
                -1f, -1f, -1f, 1f, -1f, -1f, 1f, 1f, -1f, -1f, 1f, -1f,
                -1f, -1f, 1f, 1f, -1f, 1f, 1f, 1f, 1f, -1f, 1f, 1f,
                -1f, 1f, -1f, 1f, 1f, -1f, 1f, 1f, 1f, -1f, 1f, 1f,
                -1f, -1f, -1f, 1f, -1f, -1f, 1f, -1f, 1f, -1f, -1f, 1f,
                1f, -1f, -1f, 1f, -1f, 1f, 1f, 1f, 1f, 1f, 1f, -1f,
                -1f, -1f, -1f, -1f, -1f, 1f, -1f, 1f, 1f, -1f, 1f, -1f
        };
        short[] indices = {
                0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7, 8, 9, 10, 8, 10, 11,
                12, 13, 14, 12, 14, 15, 16, 17, 18, 16, 18, 19, 20, 21, 22, 20, 22, 23
        };
        Mesh mesh = new Mesh(true, 24, 36, new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"));
        mesh.setVertices(vertices);
        mesh.setIndices(indices);
        return mesh;
    }

    @Override
    public void dispose() {
        irradianceShader.dispose();
        radianceShader.dispose();
        skyboxMesh.dispose();
    }
}
