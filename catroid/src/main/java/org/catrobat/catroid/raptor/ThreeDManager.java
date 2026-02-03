package org.catrobat.catroid.raptor;

import static com.badlogic.gdx.graphics.g3d.particles.ParticleShader.AlignMode.Screen;
import static com.crashinvaders.vfx.effects.util.MixEffect.Method.MIX;

import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.particles.values.CylinderSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.PointSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.RangedNumericValue;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btFixedConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btGeneric6DofSpringConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btHingeConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btPoint2PointConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.BloomEffect;
import com.crashinvaders.vfx.effects.ChromaticAberrationEffect;
import com.crashinvaders.vfx.effects.CrtEffect;
import com.crashinvaders.vfx.effects.FilmGrainEffect;
import com.crashinvaders.vfx.effects.FisheyeEffect;
import com.crashinvaders.vfx.effects.FxaaEffect;
import com.crashinvaders.vfx.effects.GaussianBlurEffect;
import com.crashinvaders.vfx.effects.LensFlareEffect;
import com.crashinvaders.vfx.effects.LevelsEffect;
import com.crashinvaders.vfx.effects.MotionBlurEffect;
import com.crashinvaders.vfx.effects.NfaaEffect;
import com.crashinvaders.vfx.effects.OldTvEffect;
import com.crashinvaders.vfx.effects.RadialBlurEffect;
import com.crashinvaders.vfx.effects.VignettingEffect;
import com.crashinvaders.vfx.effects.WaterDistortionEffect;
import com.crashinvaders.vfx.effects.ZoomEffect;
import com.danvexteam.lunoscript_annotations.LunoClass;

import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.raptor.postprocessing.AutoLensFlareEffect;
import org.catrobat.catroid.raptor.postprocessing.ExposureEffect;
import org.catrobat.catroid.raptor.postprocessing.EyeAdaptationManager;
import org.catrobat.catroid.raptor.postprocessing.LinearizeEffect;
import org.catrobat.catroid.raptor.postprocessing.TonemappingEffect;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.utils.ModelPathProcessor;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import com.crashinvaders.vfx.effects.BloomEffect;

class OptimizedBloomEffect extends BloomEffect {
    private final float scaleFactor;

    public OptimizedBloomEffect(float scaleFactor) {


        this.scaleFactor = scaleFactor;
    }

    public OptimizedBloomEffect() {
        this(4f);
    }

    @Override
    public void resize(int width, int height) {



        super.resize((int)(width / scaleFactor), (int)(height / scaleFactor));
    }
}

class OptimizedGaussianBlurEffect extends GaussianBlurEffect {
    private final float scaleFactor;

    public OptimizedGaussianBlurEffect(float scaleFactor) {
        super(BlurType.Gaussian5x5);
        this.scaleFactor = scaleFactor;
    }

    @Override
    public void resize(int width, int height) {

        super.resize((int)(width / scaleFactor), (int)(height / scaleFactor));
    }
}

class OptimizedRadialBlurEffect extends RadialBlurEffect {
    private final float scaleFactor;

    public OptimizedRadialBlurEffect(int passes, float scaleFactor) {
        super(passes);
        this.scaleFactor = scaleFactor;
    }

    @Override
    public void resize(int width, int height) {
        super.resize((int)(width / scaleFactor), (int)(height / scaleFactor));
    }
}

@LunoClass
public class ThreeDManager implements Disposable {

    public ModelBatch getWireframeBatch() {
        return wireframeBatch;
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        if (vfxManager != null) {
            vfxManager.resize(width, height);
        }
        if (sceneFbo2 != null) sceneFbo2.dispose();
        sceneFbo2 = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, true);
        sceneFbo2.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        sceneFboRegion.setTexture(sceneFbo2.getColorBufferTexture());
        sceneFboRegion.flip(false, true);
    }

    public enum PhysicsState {
        NONE,
        STATIC,
        DYNAMIC,
        MESH_STATIC
    }

    public enum PhysicsShape {
        BOX,
        SPHERE,
        CAPSULE
    }

    private static class AudioAsset {
        public Sound sound = null;
        public Music music = null;
        public final String filePath;

        public AudioAsset(FileHandle fileHandle, boolean asMusic) {
            this.filePath = fileHandle.path();
            if (asMusic) {
                music = Gdx.audio.newMusic(fileHandle);
            } else {
                sound = Gdx.audio.newSound(fileHandle);
            }
        }
        public void dispose() {
            if (sound != null) sound.dispose();
            if (music != null) music.dispose();
        }
    }

    private final Map<String, AudioAsset> loadedAudioAssets = new HashMap<>();
    private final List<PlayableAudio> active3DSounds = new ArrayList<>();

    private float globalSoundVolume = 1.0f;
    private static final float MAX_HEARING_DISTANCE = 250f;

    private final Vector3 cameraRight = new Vector3();
    private final Vector3 soundToListener = new Vector3();

    private final boolean debugEnabled = false;
    private static final boolean LOG_THREED_MANAGER_DEBUG = false;

    private static class RayCastResult {
        public boolean hasHit = false;
        public String hitObjectId = "";
        public float hitDistance = -1.0f;

        public final Vector3 hitPoint = new Vector3();
        public final Vector3 hitNormal = new Vector3();
    }

    public com.badlogic.gdx.graphics.Color skyColor = new com.badlogic.gdx.graphics.Color(0, 0, 0, 0);

    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;

    private boolean editorMode = false;

    private Map<String, Model> loadedModels = new HashMap<>();

    private Map<String, Texture> loadedTextures = new HashMap<>();
    private IBLBuilderCompat iblBuilderCompat;

    private Map<String, ModelInstance> sceneObjects = new HashMap<>();

    private Map<String, DirectionalLight> directionalLights = new HashMap<>();

    private final BoundingBox bounds1 = new BoundingBox();
    private final BoundingBox bounds2 = new BoundingBox();

    private btCollisionConfiguration collisionConfig;
    private btDispatcher dispatcher;
    private btBroadphaseInterface broadphase;
    private btConstraintSolver solver;
    private btDiscreteDynamicsWorld dynamicsWorld;
    private com.badlogic.gdx.graphics.g3d.utils.ModelBuilder modelBuilder;
    private CollisionCallback collisionCallback;
    private Map<String, btRigidBody> physicsBodies = new HashMap<>();
    private Map<String, RayCastResult> rayCastResults = new HashMap<>();
    private ShaderProvider defaultShaderProvider;
    private ShaderProvider customShaderProvider;

    private final Map<String, Object> customUniforms = new HashMap<>();
    private float time = 0f;

    private ModelBatch shadowBatch;
    private com.badlogic.gdx.graphics.glutils.FrameBuffer shadowFBO;
    private PerspectiveCamera lightCamera;
    private com.badlogic.gdx.math.Matrix4 lightSpaceMatrix = new com.badlogic.gdx.math.Matrix4();
    private final int SHADOW_MAP_SIZE = 2048;
    private ShaderProvider depthShaderProvider;

    private boolean realisticMode = false;
    private net.mgsx.gltf.scene3d.scene.SceneManager sceneManager;
    private com.badlogic.gdx.graphics.Texture brdfLUT;
    private com.badlogic.gdx.graphics.Cubemap diffuseCubemap;
    private com.badlogic.gdx.graphics.Cubemap specularCubemap;
    private net.mgsx.gltf.scene3d.lights.DirectionalLightEx pbrLight;

    private DebugDrawer debugDrawer;

    private final Map<String, net.mgsx.gltf.scene3d.lights.PointLightEx> pointLights = new HashMap<>();
    private final Map<String, net.mgsx.gltf.scene3d.lights.SpotLightEx> spotLights = new HashMap<>();
    private final java.util.Set<String> gltfObjectIds = new java.util.HashSet<>();
    private final Map<String, AnimationController> animationControllers = new HashMap<>();
    private net.mgsx.gltf.scene3d.scene.SceneSkybox skybox;
    private Cubemap skyboxCubemap;
    private PanoramicConverter panoramicConverter;
    private ModelInstance gridInstance;
    private final Map<String, ModelInstance> editorProxies = new HashMap<>();
    private Model lightProxyModel;
    private ModelBatch wireframeBatch;
    private Model wireframeBoxModel;
    private Model wireframeSphereModel;
    private Model wireframeCylinderModel;
    private Model cameraProxyModel;
    public String cameraTargetId = null;
    private final Vector3 cameraOffset = new Vector3();
    private float cameraDistance = 10.0f;
    private float cameraPitch = 20.0f;
    private float cameraYaw = 0.0f;

    private Map<String, btTypedConstraint> physicsConstraints = new HashMap<>();

    private final Set<String> inactiveRenderObjects = new HashSet<>();
    private final Map<String, btRigidBody> inactivePhysicsBodies = new HashMap<>();
    private final Map<String, net.mgsx.gltf.scene3d.scene.Scene> inactivePbrScenes = new HashMap<>();

    private SceneSettings currentSettings;

    private final Vector3 tmpPos = new Vector3();
    private final Quaternion tmpRot = new Quaternion();
    private final Vector3 tmpScale = new Vector3();
    private final Vector3 tmpPos2 = new Vector3();

    private VfxManager vfxManager;
    private FrameBuffer sceneFbo2;

    public static class SceneSettings {
        public int numPointLights = 5;
        public int numSpotLights = 2;
        public int numDirectionalLights = 1;
        public int numBones = 110;
    }

    private SceneManager manager;

    public boolean postprocessingEnabled = false;

    private ParticleSystem particleSystem;

    private final Map<String, ParticleEffect> activeParticleEffects = new HashMap<>();
    private Texture defaultParticleTexture;

    private final List<ParticleEffect> effectsNormal = new ArrayList<>();
    private final List<ParticleEffect> effectsAdditive = new ArrayList<>();

    private ModelBatch particleModelBatch;
    private Model particleProxyModel;
    private com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch batchNormal;
    private com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch batchAdditive;

    private final Map<String, BillboardParticleBatch> managedBatches = new HashMap<>();
    private EyeAdaptationManager eyeAdaptationManager;
    private ExposureEffect exposureEffect;
    private TonemappingEffect tonemappingEffect;
    private LinearizeEffect linearizeEffect;
    private final List<com.crashinvaders.vfx.effects.VfxEffect> userEffects = new ArrayList<>();

    private FogComponent currentFog;
    private ShaderProgram fogShader;
    private Mesh fullscreenQuad;
    private final Matrix4 inverseProjectionView = new Matrix4();
    private final java.util.Set<String> hiddenSpawnIds = new java.util.HashSet<>();

    public void init() {
        init(new SceneSettings());
    }

    public void init(SceneSettings settings) {
        this.currentSettings = settings;
        modelBuilder = new com.badlogic.gdx.graphics.g3d.utils.ModelBuilder();
        defaultShaderProvider = new DefaultShaderProvider();
        modelBatch = new ModelBatch(defaultShaderProvider);
        environment = new Environment();
        setAmbientLight(0.4f, 0.4f, 0.4f);

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        setCameraPosition(100f, 100f, 100f);
        cameraLookAt(0, 0, 0);

        camera.near = 0.1f;
        camera.far = 2500f;

        shadowFBO = new com.badlogic.gdx.graphics.glutils.FrameBuffer(com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, true);

        lightCamera = new PerspectiveCamera(90, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE);
        lightCamera.near = 1f;
        lightCamera.far = 500f;

        String depthVertexShader = "attribute vec3 a_position; uniform mat4 u_projViewTrans; void main() { gl_Position = u_projViewTrans * vec4(a_position, 1.0); }";
        String depthFragmentShader = "#ifdef GL_ES\n" +
                "precision highp float;\n" +
                "#endif\n" +
                "void main() {\n" +
                "    float depth = gl_FragCoord.z;\n" +
                "    gl_FragColor = vec4(depth, 0.0, 0.0, 1.0);\n" +
                "}\n";
        depthShaderProvider = new DefaultShaderProvider(depthVertexShader, depthFragmentShader);
        shadowBatch = new ModelBatch(depthShaderProvider);

        Bullet.init();
        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();
        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfig);
        dynamicsWorld.setGravity(new Vector3(0, -9.81f, 0));

        debugDrawer = new DebugDrawer();
        debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);
        dynamicsWorld.setDebugDrawer(debugDrawer);
        collisionCallback = new CollisionCallback();

        PBRShaderProvider prov = PBRShaderProvider.createDefault(settings.numBones);
        DefaultShader.Config config = prov.config;
        config.numPointLights = settings.numPointLights;
        config.numSpotLights = settings.numSpotLights;
        config.numDirectionalLights = settings.numDirectionalLights;
        config.numBones = settings.numBones;

        sceneManager = new net.mgsx.gltf.scene3d.scene.SceneManager(prov, PBRShaderProvider.createDefaultDepth(config.numBones));
        sceneManager.setCamera(camera);
        net.mgsx.gltf.scene3d.lights.DirectionalShadowLight shadowLight = new net.mgsx.gltf.scene3d.lights.DirectionalShadowLight(2048, 2048);
        shadowLight.direction.set(1, -1.5f, 1).nor();
        shadowLight.color.set(com.badlogic.gdx.graphics.Color.WHITE);
        shadowLight.intensity = 5.0f;

        shadowLight.getCamera().far = 1000f;

        sceneManager.environment.add(shadowLight);

        pbrLight = shadowLight;

        net.mgsx.gltf.scene3d.utils.IBLBuilder iblBuilder = net.mgsx.gltf.scene3d.utils.IBLBuilder.createOutdoor(pbrLight);

        diffuseCubemap = iblBuilder.buildIrradianceMap(256);
        specularCubemap = iblBuilder.buildRadianceMap(10);
        iblBuilder.dispose();

        brdfLUT = new com.badlogic.gdx.graphics.Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

        sceneManager.environment.set(new net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        sceneManager.environment.set(new net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute(net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute.DiffuseEnv, diffuseCubemap));
        sceneManager.environment.set(new net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute(net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute.SpecularEnv, specularCubemap));

        dynamicsWorld.getSolverInfo().setNumIterations(20);

        lightProxyModel = modelBuilder.createSphere(0.25f, 0.25f, 0.25f, 8, 8, 
                new com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(com.badlogic.gdx.graphics.Color.YELLOW)),
                com.badlogic.gdx.graphics.VertexAttributes.Usage.Position | com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal);
        particleProxyModel = modelBuilder.createSphere(0.25f, 0.25f, 0.25f, 8, 8,
                new com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(com.badlogic.gdx.graphics.Color.MAGENTA)),
                com.badlogic.gdx.graphics.VertexAttributes.Usage.Position | com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal);

        Material camMat = new Material(ColorAttribute.createDiffuse(Color.CYAN));
        Model box = modelBuilder.createBox(0.4f, 0.4f, 0.4f, camMat, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        Model cone = modelBuilder.createCone(0.4f, 0.5f, 0.4f, 4, camMat, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        ModelBuilder mb = new ModelBuilder();
        mb.begin();

        mb.part("box", box.meshes.get(0), GL20.GL_TRIANGLES, camMat);

        Node coneNode = mb.node(); 
        coneNode.id = "cone";
        
        coneNode.rotation.set(Vector3.X, 90);
        coneNode.translation.set(0, 0, -0.2f); 

        mb.part("cone", cone.meshes.get(0), GL20.GL_TRIANGLES, camMat);

        cameraProxyModel = mb.end();

        wireframeBatch = new ModelBatch();
        Material wireframeMaterial = new Material(ColorAttribute.createDiffuse(Color.WHITE));
        long usage = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        wireframeBoxModel = modelBuilder.createBox(1f, 1f, 1f, GL20.GL_LINES, wireframeMaterial, usage);
        wireframeSphereModel = modelBuilder.createSphere(1f, 1f, 1f, 16, 12, GL20.GL_LINES, wireframeMaterial, usage);
        wireframeCylinderModel = modelBuilder.createCylinder(1f, 2f, 1f, 16, GL20.GL_LINES, wireframeMaterial, usage);

        vfxManager = new VfxManager(Pixmap.Format.RGBA8888);
        sceneFbo2 = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        sceneFbo2.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        sceneFboRegion = new TextureRegion(sceneFbo2.getColorBufferTexture());
        sceneFboRegion.flip(false, true);

        eyeAdaptationManager = new EyeAdaptationManager();

        currentConfig.isActive = false;
        currentConfig.effects.clear();
        currentConfig.qualityScale = 1.0f;
        updatePostProcessing(currentConfig);



        //createGuaranteedTestEffect();

        panoramicConverter = new PanoramicConverter();
        iblBuilderCompat = new IBLBuilderCompat();
        blitBatch = new SpriteBatch();
        blitCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        blitCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        createDefaultParticleTexture();

        contactListCallback = new NameAccumulatingContactCallback();

        fogShader = new ShaderProgram(
                Gdx.files.internal("shaders/fog.vs.glsl"),
                Gdx.files.internal("shaders/fog.fs.glsl")
        );
        if (!fogShader.isCompiled()) {
            Gdx.app.error("FogShader", "Compilation failed:\n" + fogShader.getLog());
        }

        fullscreenQuad = new Mesh(true, 4, 6,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0")
        );
        fullscreenQuad.setVertices(new float[] {
                -1f, -1f, 0f, 0f,
                1f, -1f, 1f, 0f,
                1f,  1f, 1f, 1f,
                -1f,  1f, 0f, 1f
        });
        fullscreenQuad.setIndices(new short[] { 0, 1, 2, 2, 3, 0 });
    }

    public void flagObjectForHiddenSpawn(String objectId) {
        hiddenSpawnIds.add(objectId);
    }

    public void createParticleProxy(String ownerId) {
        if (editorProxies.containsKey(ownerId) || particleProxyModel == null) return;
        ModelInstance proxyInstance = new ModelInstance(particleProxyModel);
        editorProxies.put(ownerId, proxyInstance);
    }

    public void setFog(FogComponent fog) {
        this.currentFog = fog;
    }



    private BillboardParticleBatch getBatchFor(Texture texture, boolean isAdditive) {
        ensureParticleSystemInitialized();
        String key = texture.getTextureObjectHandle() + (isAdditive ? "_ADD" : "_NRM");

        BillboardParticleBatch batch = managedBatches.get(key);

        if (batch == null) {
            Gdx.app.log("ParticleBatchManager", "Creating new batch for key: " + key);

            BlendingAttribute blendingAttribute;
            DepthTestAttribute depthTestAttribute;

            if (isAdditive) {
                blendingAttribute = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE, 1f);
                depthTestAttribute = new DepthTestAttribute(GL20.GL_LEQUAL, false);
            } else {
                blendingAttribute = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f);
                depthTestAttribute = new DepthTestAttribute(GL20.GL_LEQUAL, true);
            }

            batch = new BillboardParticleBatch(ParticleShader.AlignMode.Screen, true, 1000, blendingAttribute, depthTestAttribute);
            batch.setCamera(camera);
            batch.setTexture(texture);

            particleSystem.add(batch);
            managedBatches.put(key, batch);
        }


        batch.setTexture(texture);
        return batch;
    }

    private void updateParticles(float delta) {
        if (!particleSystemInitialized || activeParticleEffects.isEmpty()) return;
        if (activeParticleEffects.isEmpty()) return;


        particleSystem.begin();


        for (ParticleEffect effect : activeParticleEffects.values()) {
            effect.update(delta);
            effect.draw();
        }


        particleSystem.end();
    }

    public void createGuaranteedTestEffect() {
        Gdx.app.postRunnable(() -> {
            if (defaultParticleTexture == null) {
                Gdx.app.error("PARTICLE_DEBUG", "Default particle texture is null!");
                return;
            }

            BillboardParticleBatch batch = getBatchFor(defaultParticleTexture, false);

            ParticleEffect effect = new ParticleEffect();
            RegularEmitter emitter = new RegularEmitter();
            BillboardRenderer renderer = new BillboardRenderer(batch);

            com.badlogic.gdx.graphics.g3d.particles.ParticleController controller =
                    new com.badlogic.gdx.graphics.g3d.particles.ParticleController("GUARANTEE_TEST", emitter, renderer);


            emitter.setContinuous(true);
            emitter.getDuration().setLow(3000);
            emitter.getEmission().setHigh(50);
            emitter.getLife().setHigh(3000);
            emitter.setMaxParticleCount(500);


            controller.influencers.add(new RegionInfluencer.Single(new TextureRegion(defaultParticleTexture)));
            ScaleInfluencer scale = new ScaleInfluencer();
            scale.value.setHigh(5f, 10f);
            controller.influencers.add(scale);
            ColorInfluencer.Single col = new ColorInfluencer.Single();
            col.colorValue.setColors(new float[]{1, 0, 0});
            col.alphaValue.setHigh(1f);
            controller.influencers.add(col);
            PointSpawnShapeValue pointSpawn = new PointSpawnShapeValue();
            controller.influencers.add(new com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnInfluencer(pointSpawn));


            effect.getControllers().add(controller);
            effect.init();
            effect.start();
            effect.setTransform(new Matrix4().setToTranslation(0, 5, 0));

            particleSystem.add(effect);
            activeParticleEffects.put("GUARANTEE_TEST", effect);
            Gdx.app.log("PARTICLE_DEBUG", "GUARANTEE_TEST effect created and started using managed batch.");
        });
    }

    private void createDefaultParticleTexture() {
        Gdx.app.log("PARTICLE_DEBUG", "Creating default white gradient texture...");
        int size = 64;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float centerX = size / 2f;
        float centerY = size / 2f;
        float maxRadius = size / 2f;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                float dx = x - centerX;
                float dy = y - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);


                float alpha = 1.0f - (distance / maxRadius);
                alpha = Math.max(0, alpha);


                alpha = 1.0f - (1.0f - alpha) * (1.0f - alpha);

                pixmap.setColor(1, 1, 1, alpha);
                pixmap.drawPixel(x, y);
            }
        }

        defaultParticleTexture = new Texture(pixmap);
        pixmap.dispose();
        Gdx.app.log("PARTICLE_DEBUG", "Default texture created. Handle: " + defaultParticleTexture.getTextureObjectHandle());
    }

    private boolean particleSystemInitialized = false;

    private void ensureParticleSystemInitialized() {
        if (particleSystemInitialized) return;

        Gdx.app.log("ParticleSystem", "First particle effect created. Initializing system...");
        particleModelBatch = new ModelBatch(new DefaultShaderProvider() {
            @Override
            protected Shader createShader(Renderable renderable) {
                return new ParticleShader(renderable, new ParticleShader.Config());
            }
        });

        particleSystem = new ParticleSystem();


        batchNormal = new com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch();
        batchNormal.setCamera(camera);
        particleSystem.add(batchNormal);

        batchAdditive = new com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch();
        batchAdditive.setCamera(camera);
        particleSystem.add(batchAdditive);

        particleSystemInitialized = true;

        // createGuaranteedTestEffect();
    }


    /*public void updateParticleEffect(String objectId, ParticleComponent data, Matrix4 transform) {
        Gdx.app.log("ParticleLifecycle", "--- Received update request for effect: " + objectId + " ---");
        removeParticleEffect(objectId);

        Gdx.app.postRunnable(() -> {
            Gdx.app.log("ParticleLifecycle", "Runnable: Starting creation for " + objectId);

            Texture texture = defaultParticleTexture;
            if (data.texturePath != null && !data.texturePath.isEmpty()) {
                Texture customTex = loadTexture(data.texturePath);
                if (customTex != null) texture = customTex;
            }
            if (texture == null) {
                Gdx.app.error("ParticleLifecycle", "Runnable: FAILED. Texture is null for " + objectId);
                return;
            }

            BillboardParticleBatch targetBatch = getBatchFor(texture, data.isAdditive);

            ParticleEffect effect = new ParticleEffect();
            RegularEmitter emitter = new RegularEmitter();
            BillboardRenderer renderer = new BillboardRenderer(targetBatch);
            com.badlogic.gdx.graphics.g3d.particles.ParticleController controller =
                    new com.badlogic.gdx.graphics.g3d.particles.ParticleController("PC_" + objectId, emitter, renderer);


            emitter.setContinuous(data.looping);
            emitter.getDuration().setLow(data.duration * 1000f);
            emitter.getEmission().setHigh(data.emissionRate);
            emitter.getLife().setHigh(data.startLifetime * 1000f);
            emitter.setMaxParticleCount(data.maxParticles);




            CylinderSpawnShapeValue spawnShape = new CylinderSpawnShapeValue();
            spawnShape.setDimensions(data.coneRadius * 2, 0.01f, data.coneRadius * 2);
            spawnShape.setEdges(true);
            controller.influencers.add(new SpawnInfluencer(spawnShape));


            DynamicsInfluencer dynamicsInfluencer = new DynamicsInfluencer();


            DynamicsModifier.PolarAcceleration upwardVelocity = new DynamicsModifier.PolarAcceleration();
            upwardVelocity.thetaValue.setHigh(0, data.coneAngle);
            upwardVelocity.phiValue.setHigh(0, 360);
            upwardVelocity.strengthValue.setHigh(data.startSpeed);

            upwardVelocity.strengthValue.setTimeline(new float[] {0, 0.1f, 0.75f, 1});
            upwardVelocity.strengthValue.setScaling(new float[] {1, 0.5f, 0.2f, 0});
            dynamicsInfluencer.velocities.add(upwardVelocity);


            DynamicsModifier.BrownianAcceleration turbulence = new DynamicsModifier.BrownianAcceleration();

            turbulence.strengthValue.setHigh(data.startSpeed * 0.5f);
            dynamicsInfluencer.velocities.add(turbulence);


            if (data.gravityModifier != 0) {
                DynamicsModifier.PolarAcceleration gravityForce = new DynamicsModifier.PolarAcceleration();
                gravityForce.thetaValue.setHigh(180);
                gravityForce.strengthValue.setHigh(9.81f * data.gravityModifier);
                dynamicsInfluencer.velocities.add(gravityForce);
            }
            controller.influencers.add(dynamicsInfluencer);


            controller.influencers.add(new RegionInfluencer.Single(new TextureRegion(texture)));


            ColorInfluencer.Single colorInfluencer = new ColorInfluencer.Single();

            colorInfluencer.colorValue.setColors(new float[] {data.startColor.r, data.startColor.g, data.startColor.b, data.endColor.r, data.endColor.g, data.endColor.b});
            colorInfluencer.colorValue.setTimeline(new float[] {0, 1});



            colorInfluencer.alphaValue.setHigh(1f);
            colorInfluencer.alphaValue.setLow(0f);


            colorInfluencer.alphaValue.setTimeline(new float[] {0, 1});
            colorInfluencer.alphaValue.setScaling(new float[] {data.startColor.a, data.endColor.a});

            controller.influencers.add(colorInfluencer);


            ScaleInfluencer scaleInfluencer = new ScaleInfluencer();
            scaleInfluencer.value.setTimeline(new float[] {0, 1});
            scaleInfluencer.value.setScaling(new float[] {1, data.endSize / data.startSize});
            scaleInfluencer.value.setHigh(data.startSize);
            controller.influencers.add(scaleInfluencer);

            if (data.rotationOverLifetime != 0 || data.startRotation != 0) {
                DynamicsModifier.Rotational2D rotation = new DynamicsModifier.Rotational2D();

                float rotationSpeed = data.rotationOverLifetime / data.startLifetime;
                rotation.strengthValue.setHigh(rotationSpeed);
                dynamicsInfluencer.velocities.add(rotation);

                if (data.startRotation != 0) {

                }
            }


            effect.getControllers().add(controller);
            effect.init();
            effect.start();
            effect.setTransform(transform);

            particleSystem.add(effect);
            activeParticleEffects.put(objectId, effect);

            Gdx.app.log("ParticleLifecycle", "Runnable: Effect " + objectId + " created successfully using composite dynamics.");
        });
    }*/

    public void updateParticleEffect(String objectId, ParticleComponent data, Matrix4 transform) {
        removeParticleEffect(objectId);
        data.migrateOldDataIfNeeded();
        data.sortGraphs();

        Gdx.app.postRunnable(() -> {
            Texture texture = defaultParticleTexture;
            if (data.texturePath != null && !data.texturePath.isEmpty()) {
                Texture customTex = loadTexture(data.texturePath);
                if (customTex != null) texture = customTex;
            }
            if (texture == null) return;

            BillboardParticleBatch targetBatch = getBatchFor(texture, data.isAdditive);
            RegularEmitter emitter = new RegularEmitter();
            BillboardRenderer renderer = new BillboardRenderer(targetBatch);
            com.badlogic.gdx.graphics.g3d.particles.ParticleController controller =
                    new com.badlogic.gdx.graphics.g3d.particles.ParticleController("PC_" + objectId, emitter, renderer);

            emitter.setContinuous(data.looping);
            emitter.getDuration().setLow(data.duration * 1000f);
            emitter.getEmission().setHigh(data.emissionRate);
            emitter.getLife().setHigh(data.startLifetime * 1000f);
            emitter.setMaxParticleCount(data.maxParticles);

            com.badlogic.gdx.graphics.g3d.particles.values.PrimitiveSpawnShapeValue shapeValue;

            switch (data.spawnShape) {
                case POINT:
                    shapeValue = new com.badlogic.gdx.graphics.g3d.particles.values.PointSpawnShapeValue();
                    break;
                case BOX:
                    com.badlogic.gdx.graphics.g3d.particles.values.RectangleSpawnShapeValue rect =
                            new com.badlogic.gdx.graphics.g3d.particles.values.RectangleSpawnShapeValue();
                    rect.setDimensions(data.spawnSize.x * 2, data.spawnSize.y * 2, data.spawnSize.z * 2);
                    shapeValue = rect;
                    break;
                case SPHERE:
                    com.badlogic.gdx.graphics.g3d.particles.values.EllipseSpawnShapeValue ellipse =
                            new com.badlogic.gdx.graphics.g3d.particles.values.EllipseSpawnShapeValue();
                    ellipse.setDimensions(data.spawnSize.x * 2, data.spawnSize.y * 2, data.spawnSize.z * 2);
                    shapeValue = ellipse;
                    break;
                case CYLINDER:
                default:
                    com.badlogic.gdx.graphics.g3d.particles.values.CylinderSpawnShapeValue cyl =
                            new com.badlogic.gdx.graphics.g3d.particles.values.CylinderSpawnShapeValue();
                    cyl.setDimensions(data.spawnSize.x * 2, data.spawnSize.y, data.spawnSize.z * 2);
                    shapeValue = cyl;
                    break;
            }

            shapeValue.setEdges(data.spawnOnSurface);

            controller.influencers.add(new com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnInfluencer(shapeValue));

            DynamicsInfluencer dynamics = new DynamicsInfluencer();

            if (!data.speedGraph.isEmpty()) {
                DynamicsModifier.PolarAcceleration velocity = new DynamicsModifier.PolarAcceleration();

                velocity.phiValue.setHigh(0, 360);
                velocity.thetaValue.setHigh(0, data.coneAngle);

                applyFloatGraph(velocity.strengthValue, data.speedGraph);
                dynamics.velocities.add(velocity);
            }

            if (!data.gravityGraph.isEmpty()) {
                DynamicsModifier.PolarAcceleration gravity = new DynamicsModifier.PolarAcceleration();
                gravity.thetaValue.setHigh(180);
                applyFloatGraph(gravity.strengthValue, data.gravityGraph);
                dynamics.velocities.add(gravity);
            }

            if (!data.vortexGraph.isEmpty()) {
                DynamicsModifier.TangentialAcceleration vortex = new DynamicsModifier.TangentialAcceleration();
                vortex.phiValue.setHigh(0);
                vortex.thetaValue.setHigh(90);
                applyFloatGraph(vortex.strengthValue, data.vortexGraph);
                dynamics.velocities.add(vortex);
            }

            if (!data.turbulenceGraph.isEmpty()) {
                DynamicsModifier.BrownianAcceleration noise = new DynamicsModifier.BrownianAcceleration();
                applyFloatGraph(noise.strengthValue, data.turbulenceGraph);
                dynamics.velocities.add(noise);
            }

            if (!data.rotationGraph.isEmpty()) {
                DynamicsModifier.Rotational2D rot = new DynamicsModifier.Rotational2D();
                applyFloatGraph(rot.strengthValue, data.rotationGraph);
                dynamics.velocities.add(rot);
            }

            controller.influencers.add(dynamics);

            ColorInfluencer.Single colorInf = new ColorInfluencer.Single();
            applyColorGraph(colorInf, data.colorGraph);
            controller.influencers.add(colorInf);

            ScaleInfluencer scaleInf = new ScaleInfluencer();
            applyFloatGraph(scaleInf.value, data.sizeGraph);
            scaleInf.value.setHigh(data.baseSize);
            controller.influencers.add(scaleInf);

            controller.influencers.add(new RegionInfluencer.Single(new TextureRegion(texture)));

            ParticleEffect effect = new ParticleEffect();
            effect.getControllers().add(controller);
            effect.init();
            effect.start();
            effect.setTransform(transform);

            particleSystem.add(effect);
            activeParticleEffects.put(objectId, effect);
        });
    }

    private void applyFloatGraph(com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue libgdxVal,
                                 List<ParticleCurvePoint<Float>> graph) {
        if (graph.isEmpty()) return;

        float[] timeline = new float[graph.size()];
        float[] values = new float[graph.size()];

        for (int i = 0; i < graph.size(); i++) {
            timeline[i] = graph.get(i).time;
            values[i] = graph.get(i).value;
        }

        libgdxVal.setTimeline(timeline);
        libgdxVal.setScaling(values);
        libgdxVal.setHigh(1f);
    }

    private void applyColorGraph(ColorInfluencer.Single colorInf, List<ParticleCurvePoint<Color>> graph) {
        if (graph.isEmpty()) return;

        float[] timeline = new float[graph.size()];
        float[] colors = new float[graph.size() * 3];
        float[] alphas = new float[graph.size()];

        for (int i = 0; i < graph.size(); i++) {
            ParticleCurvePoint<Color> p = graph.get(i);
            timeline[i] = p.time;

            colors[i*3] = p.value.r;
            colors[i*3+1] = p.value.g;
            colors[i*3+2] = p.value.b;

            alphas[i] = p.value.a;
        }

        colorInf.colorValue.setTimeline(timeline);
        colorInf.colorValue.setColors(colors);

        colorInf.alphaValue.setTimeline(timeline);
        colorInf.alphaValue.setScaling(alphas);
        colorInf.alphaValue.setHigh(1f);
    }


    public void removeParticleEffect(String objectId) {
        ParticleEffect effect = activeParticleEffects.remove(objectId);
        if (effect != null) {
            Gdx.app.log("ParticleLifecycle", "Removing effect: " + objectId);
            particleSystem.remove(effect);
            effect.dispose();
        }
    }


    public void updateParticleTransform(String objectId, Matrix4 transform) {
        ParticleEffect effect = activeParticleEffects.get(objectId);
        if (effect != null) {
            //Gdx.app.log("ParticleTransform", "Updating transform for " + objectId);
            effect.setTransform(transform);
        }
    }


    public PostProcessingComponent currentConfig = new PostProcessingComponent();

    public void updatePostProcessing(PostProcessingComponent config) {
        this.currentConfig = config;
        Gdx.app.postRunnable(() -> {
            if (vfxManager == null) return;

            this.postprocessingEnabled = config.isActive;

            if (!config.isActive) {
                vfxManager.removeAllEffects();
                userEffects.clear();
                return;
            }

            int w = Gdx.graphics.getWidth();
            int h = Gdx.graphics.getHeight();

            float scale = Math.max(0.01f, Math.min(1.0f, config.qualityScale));

            if (vfxManager.getWidth() != (int) (w * scale) || vfxManager.getHeight() != (int) (h * scale)) {
                vfxManager.resize((int) (w * scale), (int) (h * scale));
            }

            vfxManager.removeAllEffects();
            userEffects.clear();

            boolean tonemappingOrAdaptationActive =
                    config.hasEffect(PostProcessingData.ACES.class) ||
                            config.hasEffect(PostProcessingData.EyeAdaptation.class);

            if (tonemappingOrAdaptationActive) {
                if (linearizeEffect == null) linearizeEffect = new LinearizeEffect();
                vfxManager.addEffect(linearizeEffect);
            }

            if (config.hasEffect(PostProcessingData.EyeAdaptation.class)) {
                if (exposureEffect == null) exposureEffect = new ExposureEffect();
                vfxManager.addEffect(exposureEffect);
            }

            for (PostProcessingData data : config.effects) {
                if (!data.isEnabled) continue;

                if (data instanceof PostProcessingData.Bloom) {
                    PostProcessingData.Bloom b = (PostProcessingData.Bloom) data;

                    BloomEffect effect = new OptimizedBloomEffect(b.size);



                    effect.setBlurPasses(b.blurPasses);



                    effect.setBlurAmount(b.blurAmount);

                    effect.setThreshold(b.threshold);
                    effect.setBloomIntensity(b.intensity);

                    vfxManager.addEffect(effect);
                } else if (data instanceof PostProcessingData.Vignette) {
                    PostProcessingData.Vignette v = (PostProcessingData.Vignette) data;
                    VignettingEffect effect = new VignettingEffect(false);
                    effect.setIntensity(v.intensity);
                    effect.setSaturation(v.saturation);
                    vfxManager.addEffect(effect);
                } else if (data instanceof PostProcessingData.Levels) {
                    PostProcessingData.Levels l = (PostProcessingData.Levels) data;
                    LevelsEffect effect = new LevelsEffect();
                    effect.setContrast(l.contrast);
                    effect.setSaturation(l.saturation);
                    effect.setGamma(l.gamma);
                    vfxManager.addEffect(effect);
                } else if (data instanceof PostProcessingData.Grain) {
                    PostProcessingData.Grain g = (PostProcessingData.Grain) data;
                    FilmGrainEffect effect = new FilmGrainEffect();
                    effect.setNoiseAmount(g.amount);
                    vfxManager.addEffect(effect);
                } else if (data instanceof PostProcessingData.Chromatic) {
                    PostProcessingData.Chromatic c = (PostProcessingData.Chromatic) data;
                    ChromaticAberrationEffect effect = new ChromaticAberrationEffect((int) c.maxDistortion);
                    effect.setMaxDistortion(c.strength);
                    vfxManager.addEffect(effect);
                } else if (data instanceof PostProcessingData.Fxaa) {
                    vfxManager.addEffect(new FxaaEffect());
                } else if (data instanceof PostProcessingData.RadialBlur) {
                    PostProcessingData.RadialBlur r = (PostProcessingData.RadialBlur) data;

                    RadialBlurEffect effect = new OptimizedRadialBlurEffect(r.blurPasses, r.size);
                    effect.setStrength(r.strength);
                    vfxManager.addEffect(effect);
                }
                else if (data instanceof PostProcessingData.OldTv) {
                    PostProcessingData.OldTv tv = (PostProcessingData.OldTv) data;
                    OldTvEffect effect = new OldTvEffect();
                    vfxManager.addEffect(effect);
                }
                else if (data instanceof PostProcessingData.Gaussian) {
                    PostProcessingData.Gaussian g = (PostProcessingData.Gaussian) data;
                    GaussianBlurEffect effect = new OptimizedGaussianBlurEffect(g.size);
                    effect.setPasses(g.passes);
                    effect.setAmount(g.amount);
                    vfxManager.addEffect(effect);
                }
                else if (data instanceof PostProcessingData.Zoom) {
                    PostProcessingData.Zoom z = (PostProcessingData.Zoom) data;
                    ZoomEffect effect = new ZoomEffect();
                    effect.setZoom(z.zoom);
                    effect.setOrigin(z.originX, z.originY);
                    vfxManager.addEffect(effect);
                }
                else if (data instanceof PostProcessingData.Crt) {
                    PostProcessingData.Crt c = (PostProcessingData.Crt) data;
                    CrtEffect effect = new CrtEffect();
                    vfxManager.addEffect(effect);
                }
                else if (data instanceof PostProcessingData.Fisheye) {
                    PostProcessingData.Fisheye f = (PostProcessingData.Fisheye) data;
                    FisheyeEffect effect = new FisheyeEffect();
                    vfxManager.addEffect(effect);
                }
                else if (data instanceof PostProcessingData.Water) {
                    PostProcessingData.Water w2 = (PostProcessingData.Water) data;
                    WaterDistortionEffect effect = new WaterDistortionEffect(w2.amount, w2.speed);
                    vfxManager.addEffect(effect);
                }
                else if (data instanceof PostProcessingData.MotionBlur) {
                    PostProcessingData.MotionBlur mb = (PostProcessingData.MotionBlur) data;

                    MotionBlurEffect effect = new MotionBlurEffect(Pixmap.Format.RGBA8888, MIX, mb.blurOpacity);
                    vfxManager.addEffect(effect);
                }
                else if (data instanceof PostProcessingData.LensFlare) {
                    PostProcessingData.LensFlare lf = (PostProcessingData.LensFlare) data;

                    AutoLensFlareEffect effect = new AutoLensFlareEffect();




                    effect.setThreshold(lf.threshold);



                    effect.setIntensity(lf.intensity * 2.0f);


                    effect.setDispersal(lf.dispersal);
                    effect.setSize(lf.size);


                    vfxManager.addEffect(effect);
                }
            }

            if (config.hasEffect(PostProcessingData.ACES.class)) {
                if (tonemappingEffect == null) tonemappingEffect = new TonemappingEffect();
                vfxManager.addEffect(tonemappingEffect);
            }
        });
    }

    public void setObjectVisibility(String objectId, boolean visible) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance == null) return;

        if (visible) {
            btRigidBody body = inactivePhysicsBodies.remove(objectId);
            if (body != null) {
                dynamicsWorld.addRigidBody(body);
                physicsBodies.put(objectId, body);
            }
        } else {
            btRigidBody body = physicsBodies.remove(objectId);
            if (body != null) {
                dynamicsWorld.removeRigidBody(body);
                inactivePhysicsBodies.put(objectId, body);
            }
        }

        if (visible) {
            inactiveRenderObjects.remove(objectId);
            if (realisticMode && inactivePbrScenes.containsKey(objectId)) {
                sceneManager.addScene(inactivePbrScenes.remove(objectId));
            }
        } else {
            inactiveRenderObjects.add(objectId);
            if (realisticMode) {
                net.mgsx.gltf.scene3d.scene.Scene sceneToRemove = null;
                for (RenderableProvider provider : sceneManager.getRenderableProviders()) {
                    if (provider instanceof net.mgsx.gltf.scene3d.scene.Scene) {
                        net.mgsx.gltf.scene3d.scene.Scene scene = (net.mgsx.gltf.scene3d.scene.Scene) provider;
                        if (scene.modelInstance == instance) {
                            sceneToRemove = scene;
                            break;
                        }
                    }
                }
                if (sceneToRemove != null) {
                    sceneManager.removeScene(sceneToRemove);
                    inactivePbrScenes.put(objectId, sceneToRemove);
                }
            }
        }
    }

    public void setSceneManager(SceneManager manager) {
        this.manager = manager;
    }


    @Deprecated
    public void setObjectRenderState(String objectId, boolean active) {
        setObjectVisibility(objectId, active);
    }

    public void setObjectPhysicsState(String objectId, boolean active) {
        if (active) {
            btRigidBody body = inactivePhysicsBodies.remove(objectId);
            if (body != null) {
                dynamicsWorld.addRigidBody(body);
                physicsBodies.put(objectId, body);
            }
        } else {
            btRigidBody body = physicsBodies.remove(objectId);
            if (body != null) {
                dynamicsWorld.removeRigidBody(body);
                inactivePhysicsBodies.put(objectId, body);
            }
        }
    }

    public SceneSettings getSceneSettings() {
        return currentSettings != null ? currentSettings : new SceneSettings();
    }

    public void setCameraFov(float fieldOfView, float near, float far) {
        camera.fieldOfView = fieldOfView;
        camera.near = near;
        camera.far = far;
        camera.update();
    }

    public DebugDrawer getDebugDrawer() { return debugDrawer; }

    public void createCameraProxy(String ownerId) {
        if (editorProxies.containsKey(ownerId) || cameraProxyModel == null) return;
        ModelInstance proxyInstance = new ModelInstance(cameraProxyModel);
        editorProxies.put(ownerId, proxyInstance);
    }

    public boolean createPoint2PointConstraint(String constraintId, String objectIdA, String objectIdB, Vector3 pivotInA, Vector3 pivotInB) {
        if (physicsConstraints.containsKey(constraintId)) return false;
        btRigidBody bodyA = physicsBodies.get(objectIdA);
        btRigidBody bodyB = physicsBodies.get(objectIdB);

        if (bodyA == null || bodyB == null) {
            Gdx.app.error("3DManager", "Cannot create constraint: one or both bodies not found.");
            return false;
        }

        btPoint2PointConstraint constraint = new btPoint2PointConstraint(bodyA, bodyB, pivotInA, pivotInB);
        dynamicsWorld.addConstraint(constraint, true);
        physicsConstraints.put(constraintId, constraint);
        return true;
    }

    public boolean createFixedConstraint(String constraintId, String objectIdA, String objectIdB, Matrix4 transformA, Matrix4 transformB) {
        if (physicsConstraints.containsKey(constraintId)) return false;
        btRigidBody bodyA = physicsBodies.get(objectIdA);
        btRigidBody bodyB = physicsBodies.get(objectIdB);

        if (bodyA == null || bodyB == null) {
            Gdx.app.error("3DManager", "Cannot create fixed constraint: one or both bodies not found.");
            return false;
        }

        btFixedConstraint constraint = new btFixedConstraint(bodyA, bodyB, transformA, transformB);
        dynamicsWorld.addConstraint(constraint, true);
        physicsConstraints.put(constraintId, constraint);
        return true;
    }


    public void removeConstraint(String constraintId) {
        btTypedConstraint constraint = physicsConstraints.remove(constraintId);
        if (constraint != null) {
            dynamicsWorld.removeConstraint(constraint);
            constraint.dispose();
        }
    }


    private final Vector3 cyanColorVec = new Vector3(Color.CYAN.r, Color.CYAN.g, Color.CYAN.b);

    

    public void renderCameraFrustum(Matrix4 ownerTransform, float fov, float far) {
        if (debugDrawer == null) return;

        PerspectiveCamera tempCam = new PerspectiveCamera(fov, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        tempCam.far = far;
        Quaternion rot = ownerTransform.getRotation(new Quaternion());
        tempCam.position.set(ownerTransform.getTranslation(new Vector3()));
        tempCam.direction.set(0, 0, -1);
        rot.transform(tempCam.direction);
        tempCam.up.set(0, 1, 0);
        rot.transform(tempCam.up);

        tempCam.update();

        Vector3[] points = tempCam.frustum.planePoints;
        
        debugDrawer.drawLine(points[4], points[5], cyanColorVec);
        debugDrawer.drawLine(points[5], points[6], cyanColorVec);
        debugDrawer.drawLine(points[6], points[7], cyanColorVec);
        debugDrawer.drawLine(points[7], points[4], cyanColorVec);
        
        debugDrawer.drawLine(points[0], points[1], cyanColorVec);
        debugDrawer.drawLine(points[1], points[2], cyanColorVec);
        debugDrawer.drawLine(points[2], points[3], cyanColorVec);
        debugDrawer.drawLine(points[3], points[0], cyanColorVec);
        
        debugDrawer.drawLine(points[0], points[4], cyanColorVec);
        debugDrawer.drawLine(points[1], points[5], cyanColorVec);
        debugDrawer.drawLine(points[2], points[6], cyanColorVec);
        debugDrawer.drawLine(points[3], points[7], cyanColorVec);
    }

    
    public void setCameraRotation(Quaternion rotation) {
        camera.direction.set(0, 0, -1);
        rotation.transform(camera.direction);
        camera.up.set(0, 1, 0);
        rotation.transform(camera.up);
        camera.update();
    }

    public void setEditorMode(boolean enabled) {
        this.editorMode = enabled;
    }

    public void setPhysicsStateFromComponent(String objectId, PhysicsComponent component) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance == null) return;

        removePhysicsBody(objectId);

        float bodyMass = (component.state == PhysicsState.DYNAMIC) ? component.mass : 0f;

        if (component.state == PhysicsState.NONE) return;

        if (component.state == PhysicsState.MESH_STATIC) {
            if (gltfObjectIds.contains(objectId)) {
                createGltfMeshPhysicsBody(objectId, instance);
            } else {
                createMeshPhysicsBody(objectId, instance);
            }
            return;
        }
        
        if (component.colliders.isEmpty()) {
            Gdx.app.error("3DManager", "setPhysicsStateFromComponent called with no colliders for object: " + objectId);
            return;
        }

        createCompoundPhysicsBody(objectId, instance, component, bodyMass);
    }

    
    private void createCompoundPhysicsBody(String objectId, ModelInstance instance, PhysicsComponent component, float mass) {
        Log.d("PhysicsDebug", "--- Creating Compound Physics Body for '" + objectId + "' ---");
        Log.d("PhysicsDebug", "  - Input Mass: " + mass);
        Log.d("PhysicsDebug", "  - Instance World Transform on entry:\n" + instance.transform);

        btCompoundShape compoundShape = new btCompoundShape();
        Array<Disposable> disposables = new Array<>();
        disposables.add(compoundShape);

        final Matrix4 worldTransform = instance.transform;
        final float[] vals = worldTransform.val;

        boolean isFlippedX = vals[Matrix4.M00] < 0;
        boolean isFlippedY = vals[Matrix4.M11] < 0;
        boolean isFlippedZ = vals[Matrix4.M22] < 0;
        Log.d("PhysicsDebug", "  - Axis flip detection: X=" + isFlippedX + ", Y=" + isFlippedY + ", Z=" + isFlippedZ);

        for (ColliderShapeData shapeData : component.colliders) {
            Vector3 correctedOffset = new Vector3(shapeData.centerOffset);
            if (isFlippedX) correctedOffset.x *= -1;
            if (isFlippedY) correctedOffset.y *= -1;
            if (isFlippedZ) correctedOffset.z *= -1;

            Log.d("PhysicsDebug", "  - Processing Collider #(тут был номер, но я его убрал)" + ": Type=" + shapeData.type + ", Local Offset=" + shapeData.centerOffset + ", Size=" + shapeData.size);
            Log.d("PhysicsDebug", "    - Original Offset: " + shapeData.centerOffset);
            Log.d("PhysicsDebug", "    - Corrected Offset for Physics: " + correctedOffset);

            btCollisionShape shape;

            switch (shapeData.type) {
                case SPHERE:
                    shape = new btSphereShape(shapeData.radius);
                    break;
                case CAPSULE:
                    float height = Math.max(0, shapeData.size.y - (shapeData.radius * 2));
                    shape = new btCapsuleShape(shapeData.radius, height);
                    break;
                case BOX:
                default:
                    shape = new btBoxShape(shapeData.size.cpy().scl(0.5f));
                    break;
            }
            disposables.add(shape);
            Matrix4 localTransform = new Matrix4().setToTranslation(correctedOffset);
            compoundShape.addChildShape(localTransform, shape);
            Log.d("PhysicsDebug", "    - Added child shape with local transform:\n" + localTransform);
        }

        Vector3 absScale = worldTransform.getScale(new Vector3());
        if (absScale.x == 0) absScale.x = 0.0001f;
        if (absScale.y == 0) absScale.y = 0.0001f;
        if (absScale.z == 0) absScale.z = 0.0001f;

        compoundShape.setLocalScaling(absScale);
        Log.d("PhysicsDebug", "  - Compound Shape Local Scaling set to (abs value): " + absScale);

        Vector3 localInertia = new Vector3();
        if (mass > 0f) {
            compoundShape.calculateLocalInertia(mass, localInertia);
        }

        Matrix4 bodyTransform = new Matrix4(worldTransform);
        bodyTransform.scale(1f / absScale.x, 1f / absScale.y, 1f / absScale.z);

        Log.d("PhysicsDebug", "  - Final RigidBody Transform for creation:\n" + bodyTransform);

        btMotionState motionState = new btDefaultMotionState(bodyTransform);
        btRigidBody.btRigidBodyConstructionInfo bodyInfo =
                new btRigidBody.btRigidBodyConstructionInfo(mass, motionState, compoundShape, localInertia);
        btRigidBody body = new btRigidBody(bodyInfo);

        if (mass > 0) {
            body.setAngularFactor(1f);
            body.setDamping(0.5f, 0.5f);
        }

        dynamicsWorld.addRigidBody(body);
        physicsBodies.put(objectId, body);

        body.userData = disposables;
        physicsResources.put(objectId, disposables);
        bodyInfo.dispose();

        Matrix4 finalBodyTransform = body.getWorldTransform();
        Log.d("PhysicsDebug", "  - SUCCESS: Body created. Final transform from physics world:\n" + finalBodyTransform);
        Log.d("PhysicsDebug", "--------------------------------------------------------");
    }

    public void setPhysicsStateFromComponent(String objectId, PhysicsComponent component, Matrix4 worldTransform) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance == null) return;

        instance.transform.set(worldTransform);

        removePhysicsBody(objectId);

        float bodyMass = (component.state == PhysicsState.DYNAMIC) ? component.mass : 0f;

        if (component.state == PhysicsState.NONE) return;

        if (component.state == PhysicsState.MESH_STATIC) {
            if (gltfObjectIds.contains(objectId)) {
                createGltfMeshPhysicsBody(objectId, instance);
            } else {
                createMeshPhysicsBody(objectId, instance);
            }
            return;
        }

        if (component.colliders.isEmpty()) {
            Gdx.app.error("3DManager", "setPhysicsStateFromComponent called with no colliders for object: " + objectId);
            return;
        }

        createCompoundPhysicsBody(objectId, instance, component, bodyMass);
    }

    private final Vector3 tmpVec = new Vector3();


    public void renderWireframeShape(ModelBatch batch, ColliderShapeData shapeData, Matrix4 parentTransform, Color color) {
        if (batch == null) return;

        ModelInstance instance;
        Matrix4 finalTransform = new Matrix4(parentTransform);

        switch (shapeData.type) {
            case BOX:
                instance = new ModelInstance(wireframeBoxModel);
                instance.transform.scl(shapeData.size);
                instance.transform.mulLeft(finalTransform.translate(shapeData.centerOffset));
                ((ColorAttribute)instance.materials.get(0).get(ColorAttribute.Diffuse)).color.set(color);
                batch.render(instance);
                break;

            case SPHERE:
                instance = new ModelInstance(wireframeSphereModel);
                instance.transform.scl(shapeData.radius * 2);
                instance.transform.mulLeft(finalTransform.translate(shapeData.centerOffset));
                ((ColorAttribute)instance.materials.get(0).get(ColorAttribute.Diffuse)).color.set(color);
                batch.render(instance);
                break;

            case CAPSULE:
                float cylinderHeight = Math.max(0, shapeData.size.y - (shapeData.radius * 2));
                Vector3 capsuleCenter = shapeData.centerOffset;
                instance = new ModelInstance(wireframeCylinderModel);
                instance.transform.scl(shapeData.radius * 2, cylinderHeight, shapeData.radius * 2);
                instance.transform.mulLeft(finalTransform.cpy().translate(capsuleCenter));
                ((ColorAttribute)instance.materials.get(0).get(ColorAttribute.Diffuse)).color.set(color);
                batch.render(instance);

                ModelInstance topSphere = new ModelInstance(wireframeSphereModel);
                tmpVec.set(capsuleCenter).add(0, cylinderHeight / 2f, 0); 
                topSphere.transform.scl(shapeData.radius * 2);
                topSphere.transform.mulLeft(finalTransform.cpy().translate(tmpVec));
                ((ColorAttribute)topSphere.materials.get(0).get(ColorAttribute.Diffuse)).color.set(color);
                batch.render(topSphere);

                ModelInstance bottomSphere = new ModelInstance(wireframeSphereModel);
                tmpVec.set(capsuleCenter).add(0, -cylinderHeight / 2f, 0); 
                bottomSphere.transform.scl(shapeData.radius * 2);
                bottomSphere.transform.mulLeft(finalTransform.cpy().translate(tmpVec));
                ((ColorAttribute)bottomSphere.materials.get(0).get(ColorAttribute.Diffuse)).color.set(color);
                batch.render(bottomSphere);
                break;
        }
    }



    public void setBackgroundLightIntensity(float intensity) {
        if (sceneManager != null) {
            ColorAttribute ambientLight = (ColorAttribute) sceneManager.environment.get(ColorAttribute.AmbientLight);
            if (ambientLight == null) {
                ambientLight = new ColorAttribute(ColorAttribute.AmbientLight, intensity, intensity, intensity, 1.0f);
                sceneManager.environment.set(ambientLight);
            } else {
                ambientLight.color.set(intensity, intensity, intensity, 1.0f);
            }
        }
    }


    public void setPointLight(String lightId, float x, float y, float z, float r, float g, float b, float intensity, float range) {
        net.mgsx.gltf.scene3d.lights.PointLightEx light = pointLights.get(lightId);
        if (light == null) {
            light = new net.mgsx.gltf.scene3d.lights.PointLightEx();
            pointLights.put(lightId, light);
            sceneManager.environment.add(light);
        }
        light.position.set(x, y, z);
        light.color.set(r, g, b, 1);
        light.intensity = intensity;
        light.range = range > 0 ? range : null;
    }

    public static float map(float value, float inMin, float inMax, float outMin, float outMax) {
        return (value - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }


    public void setSpotLight(String lightId, float x, float y, float z, float dirX, float dirY, float dirZ, float r, float g, float b, float intensity, float cutoffAngle, float exponent, float range) {
        net.mgsx.gltf.scene3d.lights.SpotLightEx light = spotLights.get(lightId);
        if (light == null) {
            light = new net.mgsx.gltf.scene3d.lights.SpotLightEx();
            spotLights.put(lightId, light);
            sceneManager.environment.add(light);
        }
        light.position.set(x, y, z);
        light.direction.set(dirX, dirY, dirZ).nor();
        light.color.set(r, g, b, 1);
        light.intensity = intensity;
        light.cutoffAngle = map(cutoffAngle, 0, 360, -1000, 1000);
        light.exponent = exponent * 1000;
        light.range = range > 0 ? range : null;
    }


    public boolean removePBRLight(String lightId) {
        net.mgsx.gltf.scene3d.lights.PointLightEx pointLight = pointLights.remove(lightId);
        if (pointLight != null) {
            sceneManager.environment.remove(pointLight);
            return true;
        }
        net.mgsx.gltf.scene3d.lights.SpotLightEx spotLight = spotLights.remove(lightId);
        if (spotLight != null) {
            sceneManager.environment.remove(spotLight);
            return true;
        }
        return false;
    }


    public void setObjectAnisotropicFilter(String objectId, float level) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance == null) return;

        for (com.badlogic.gdx.graphics.g3d.Material material : instance.materials) {
            for (com.badlogic.gdx.graphics.g3d.Attribute attr : material) {
                if (attr instanceof com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute) {
                    Texture texture = ((com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute) attr).textureDescription.texture;
                    if (texture != null) {
                        texture.setAnisotropicFilter(level);
                    }
                }
            }
        }
    }


    public void setFog(float r, float g, float b, float density) {
        ColorAttribute fogAttr = new ColorAttribute(ColorAttribute.Fog, r, g, b, 1.0f);

        if (density > 0) {
            if (sceneManager != null) {
                sceneManager.environment.set(fogAttr);
            }
            environment.set(fogAttr);
            camera.far = 1f / density;
        } else {
            camera.far = 1000f;
            if (sceneManager != null) sceneManager.environment.remove(ColorAttribute.Fog);
            environment.remove(ColorAttribute.Fog);
        }
        camera.update();
    }

    public void setAngularFactor(String objectId, float x, float y, float z) {
        btRigidBody body = physicsBodies.get(objectId);
        if (body != null) {



            body.setAngularFactor(new Vector3(x, y, z));


            body.activate();
        }
    }


    public void setSkyColor(float r, float g, float b) {
        skyColor.set(r, g, b, 1f);
    }


    public Vector3 getCameraPosition() {
        return camera.position;
    }

    public void renameObject(String oldId, String newId) {
        
        if (sceneObjects.containsKey(oldId)) {
            sceneObjects.put(newId, sceneObjects.remove(oldId));
        }
        
        if (physicsBodies.containsKey(oldId)) {
            physicsBodies.put(newId, physicsBodies.remove(oldId));
        }
        
        if (physicsResources.containsKey(oldId)) {
            physicsResources.put(newId, physicsResources.remove(oldId));
        }
        
        if (animationControllers.containsKey(oldId)) {
            animationControllers.put(newId, animationControllers.remove(oldId));
        }
        
        if (gltfObjectIds.contains(oldId)) {
            gltfObjectIds.remove(oldId);
            gltfObjectIds.add(newId);
        }
        
        if (editorProxies.containsKey(oldId)) {
            editorProxies.put(newId, editorProxies.remove(oldId));
        }
        
        if (pointLights.containsKey(oldId)) {
            pointLights.put(newId, pointLights.remove(oldId));
        }
        if (spotLights.containsKey(oldId)) {
            spotLights.put(newId, spotLights.remove(oldId));
        }
    }


    public Vector3 getCameraDirection() {
        return camera.direction;
    }

    public Matrix4 getWorldTransform(String objectId) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance != null) {
            return instance.transform;
        }
        return null;
    }

    public void setPhysicsState(String objectId, PhysicsState state, PhysicsShape shape, float mass, Matrix4 worldTransform) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance == null) return;


        instance.transform.set(worldTransform);


        setPhysicsState(objectId, state, shape, mass);
    }

    public void setShadowSettings(float size, int resolution) {
        this.currentShadowSize = Math.max(1f, size);



        if (this.currentShadowResolution != resolution) {
            this.currentShadowResolution = resolution;

            Gdx.app.postRunnable(() -> {
                recreateShadowLight();
            });
        }
    }

    public float getShadowSize() { return currentShadowSize; }
    public float getShadowResolution() { return (float) currentShadowResolution; }

    private void recreateShadowLight() {
        if (sceneManager == null) return;


        if (pbrLight != null) {
            sceneManager.environment.remove(pbrLight);
            if (pbrLight instanceof Disposable) {
                ((Disposable) pbrLight).dispose();
            }
        }


        net.mgsx.gltf.scene3d.lights.DirectionalShadowLight shadowLight =
                new net.mgsx.gltf.scene3d.lights.DirectionalShadowLight(
                        currentShadowResolution,
                        currentShadowResolution
                );




        shadowLight.direction.set(1, -1.5f, 1).nor();
        shadowLight.color.set(Color.WHITE);
        shadowLight.intensity = 5.0f;

        sceneManager.environment.add(shadowLight);
        pbrLight = shadowLight;


        updateProceduralIBL();
    }


    private void updateShadowLight() {
        if (pbrLight instanceof net.mgsx.gltf.scene3d.lights.DirectionalShadowLight) {
            net.mgsx.gltf.scene3d.lights.DirectionalShadowLight sun =
                    (net.mgsx.gltf.scene3d.lights.DirectionalShadowLight) pbrLight;


            Vector3 center = new Vector3(camera.position);



            Vector3 forward = new Vector3(camera.direction).scl(currentShadowSize * 0.4f);
            forward.y = 0;
            center.add(forward);



            center.y = 0;

            sun.setCenter(center);


            Camera shadowCam = sun.getCamera();
            shadowCam.viewportWidth = currentShadowSize;
            shadowCam.viewportHeight = currentShadowSize;




            float depthRange = currentShadowSize * 2f;


            shadowCam.near = -depthRange / 2f;
            shadowCam.far = depthRange / 2f;

            shadowCam.update();
        }
    }

    private static class BoneAttachment {
        String childId;
        String parentModelId;
        String boneName;


        Vector3 localPosOffset = new Vector3();
        Quaternion localRotOffset = new Quaternion();

        public BoneAttachment(String child, String parent, String bone) {
            this.childId = child;
            this.parentModelId = parent;
            this.boneName = bone;
        }
    }


    private final List<BoneAttachment> activeAttachments = new ArrayList<>();



    public void attachObjectToBone(String childId, String parentId, String boneName, float offsetX, float offsetY, float offsetZ) {

        detachObject(childId);

        BoneAttachment attachment = new BoneAttachment(childId, parentId, boneName);
        attachment.localPosOffset.set(offsetX, offsetY, offsetZ);

        activeAttachments.add(attachment);
    }


    public void detachObject(String childId) {
        activeAttachments.removeIf(a -> a.childId.equals(childId));
    }

    public void update(float delta) {
        if (LOG_THREED_MANAGER_DEBUG) Log.d("TDM_DEBUG", "--- ThreeDManager.update() START (Delta: " + delta + ") ---");
        if (cameraTargetId != null) {
            updateThirdPersonCamera();
        }

        updateShadowLight();

        for (com.badlogic.gdx.graphics.g3d.utils.AnimationController controller : animationControllers.values()) {
            controller.update(delta);
        }

        if (realisticMode) {
            sceneManager.update(delta);
        }

        time += delta;

        if (customShaderProvider != null) {
            customUniforms.put("u_time", time);
            customUniforms.put("u_resolution", new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
            customUniforms.put("u_cameraPosition", camera.position);
            customUniforms.put("u_cameraDirection", camera.direction);
            customUniforms.put("u_viewMatrix", camera.view);
            customUniforms.put("u_projectionMatrix", camera.projection);
        }

        if (!editorMode) {
            dynamicsWorld.stepSimulation(delta, 5, 1f / 60f);

            for (Map.Entry<String, btRigidBody> entry : physicsBodies.entrySet()) {
                String objectId = entry.getKey();
                btRigidBody body = entry.getValue();

                boolean isManagedBySceneManager = (Objects.requireNonNull(StageActivity.getActiveStageListener()).sceneManager != null && Objects.requireNonNull(StageActivity.getActiveStageListener()).sceneManager.findGameObject(objectId) != null);

                if (!isManagedBySceneManager) {
                    ModelInstance instance = sceneObjects.get(objectId);
                    if (instance != null && !body.isStaticObject() && body.isActive()) {

                        Matrix4 bodyTransform = body.getWorldTransform();

                        bodyTransform.getTranslation(tmpPos);
                        bodyTransform.getRotation(tmpRot, true);

                        instance.transform.getScale(tmpScale);

                        instance.transform.set(tmpPos, tmpRot, tmpScale);
                    }
                }
            }
        }

        if (!editorMode && LOG_THREED_MANAGER_DEBUG) {
            Log.d("TDM_DEBUG", "    [Physics] After stepSimulation:");
            for (Map.Entry<String, btRigidBody> entry : physicsBodies.entrySet()) {
                Vector3 physBodyPos = new Vector3();
                entry.getValue().getWorldTransform().getTranslation(physBodyPos);
                Log.d("TDM_DEBUG", "      - '" + entry.getKey() + "' Physics Pos: " + physBodyPos);
            }
        }
        if (!editorMode) {
            update3DAudio();
        }
        if (LOG_THREED_MANAGER_DEBUG) Log.d("TDM_DEBUG", "--- ThreeDManager.update() END ---");
    }

    private void update3DAudio() {
        if (active3DSounds.isEmpty()) {
            return;
        }

        Vector3 listenerPos = camera.position;
        cameraRight.set(camera.direction).crs(camera.up).nor();

        Iterator<PlayableAudio> iterator = active3DSounds.iterator();
        while (iterator.hasNext()) {
            PlayableAudio audio = iterator.next();

            if (!audio.isPlaying()) {
                audio.dispose();
                iterator.remove();
                continue;
            }

            String attachedId = audio.getAttachedObjectId();
            if (attachedId != null) {
                ModelInstance attachedObject = sceneObjects.get(attachedId);
                if (attachedObject != null) {
                    attachedObject.transform.getTranslation(tmpPos);
                    audio.setPosition(tmpPos);
                } else {
                    audio.stop();
                    audio.dispose();
                    iterator.remove();
                    continue;
                }
            }

            float distance = listenerPos.dst(audio.getPosition());

            float finalVolume;
            float finalPan;

            if (distance > MAX_HEARING_DISTANCE) {
                finalVolume = 0;
                finalPan = 0;
            } else {
                finalVolume = (1.0f - (distance / MAX_HEARING_DISTANCE)) * globalSoundVolume;

                soundToListener.set(audio.getPosition()).sub(listenerPos).nor();
                finalPan = soundToListener.dot(cameraRight);
            }

            audio.update3D(finalVolume, finalPan);
        }
    }

    public void updateSoundPosition(String instanceName, float x, float y, float z) {
        if (instanceName == null || instanceName.isEmpty()) return;

        for (PlayableAudio audio : active3DSounds) {
            if (instanceName.equals(audio.getInstanceName())) {
                audio.setAttachedObjectId(null);
                audio.setPosition(new Vector3(x, y, z));
                break;
            }
        }
    }

    public boolean createSpringConstraint(String constraintId, String objectIdA, String objectIdB, Matrix4 frameInA, Matrix4 frameInB) {
        if (physicsConstraints.containsKey(constraintId)) return false;
        btRigidBody bodyA = physicsBodies.get(objectIdA);

        btRigidBody bodyB = physicsBodies.get(objectIdB);
        if (bodyB == null) {

        }

        if (bodyA == null) {
            Gdx.app.error("3DManager", "Cannot create spring constraint: bodyA not found.");
            return false;
        }

        btGeneric6DofSpringConstraint springConstraint = new btGeneric6DofSpringConstraint(bodyA, bodyB, frameInA, frameInB, true);
        dynamicsWorld.addConstraint(springConstraint, true);
        physicsConstraints.put(constraintId, springConstraint);
        return true;
    }

    public boolean createHingeConstraint(String constraintId, String objectIdA, String objectIdB,
                                         Vector3 pivotInA, Vector3 axisInA,
                                         Vector3 pivotInB, Vector3 axisInB) {
        if (physicsConstraints.containsKey(constraintId)) return false;
        btRigidBody bodyA = physicsBodies.get(objectIdA);
        btRigidBody bodyB = physicsBodies.get(objectIdB);

        if (bodyA == null || bodyB == null) {
            Gdx.app.error("3DManager", "Cannot create hinge: one or both bodies not found.");
            return false;
        }
        btHingeConstraint hinge = new btHingeConstraint(bodyA, bodyB, pivotInA, pivotInB, axisInA, axisInB, true);
        dynamicsWorld.addConstraint(hinge, true);
        physicsConstraints.put(constraintId, hinge);
        return true;
    }

    public void setHingeMotorTarget(String constraintId, float targetAngleDegrees, float maxMotorImpulse) {
        btTypedConstraint constraint = physicsConstraints.get(constraintId);
        if (constraint instanceof btHingeConstraint) {
            btHingeConstraint hinge = (btHingeConstraint) constraint;
            hinge.enableAngularMotor(true, 0, maxMotorImpulse);
            hinge.setMotorTarget(targetAngleDegrees * MathUtils.degreesToRadians, 0.1f);
        }
    }

    public void setAngularSpringProperties(String constraintId, int axisIndex, float stiffness, float damping) {
        btTypedConstraint constraint = physicsConstraints.get(constraintId);
        if (constraint instanceof btGeneric6DofSpringConstraint) {
            btGeneric6DofSpringConstraint spring = (btGeneric6DofSpringConstraint) constraint;
            int bulletAxisIndex = axisIndex + 3;

            spring.enableSpring(bulletAxisIndex, true);
            spring.setStiffness(bulletAxisIndex, stiffness);
            spring.setDamping(bulletAxisIndex, damping);
        }
    }


    public btRigidBody getPhysicsBody(String objectId) {
        return physicsBodies.get(objectId);
    }

    public void setFreeCamera() {
        this.cameraTargetId = null;
    }

    public void setThirdPersonCamera(String targetObjectId, float distance, float height, float pitch) {
        ModelInstance target = sceneObjects.get(targetObjectId);
        if (target == null) {
            Gdx.app.error("3DManager", "Camera target object not found: " + targetObjectId);
            this.cameraTargetId = null;
            return;
        }
        this.cameraTargetId = targetObjectId;
        this.cameraDistance = distance;
        this.cameraOffset.set(0, height, 0);
        this.cameraPitch = pitch;

        this.cameraYaw = target.transform.getRotation(new Quaternion()).getYaw();
    }

    private void updateThirdPersonCamera() {
        ModelInstance target = sceneObjects.get(cameraTargetId);
        if (target == null) {
            setFreeCamera();
            return;
        }

        Vector3 targetPosition = target.transform.getTranslation(new Vector3());
        Vector3 lookAtPoint = new Vector3(targetPosition).add(cameraOffset);

        Quaternion rotation = new Quaternion();
        rotation.set(Vector3.Y, cameraYaw);
        rotation.mul(new Quaternion(Vector3.X, -cameraPitch));

        Vector3 positionOffset = new Vector3(0, 0, cameraDistance);
        positionOffset.mul(rotation);

        Vector3 cameraPosition = new Vector3(lookAtPoint).add(positionOffset);

        camera.position.set(cameraPosition);
        camera.lookAt(lookAtPoint);
        camera.up.set(Vector3.Y);
        camera.update();
    }

    public void addCameraRotation(float yawDelta, float pitchDelta) {
        if (cameraTargetId == null) return;

        cameraYaw += yawDelta;
        cameraPitch += pitchDelta;

        if (cameraPitch > 89.0f) cameraPitch = 89.0f;
        if (cameraPitch < -89.0f) cameraPitch = -89.0f;
    }

    public void setCameraRotation(float yaw, float pitch) {
        if (cameraTargetId == null) return;

        cameraYaw = yaw;
        cameraPitch = pitch;

        if (cameraPitch > 89.0f) cameraPitch = 89.0f;
        if (cameraPitch < -89.0f) cameraPitch = -89.0f;
    }

    public void playSoundAt(String instanceName, String soundName, float x, float y, float z, float volume, float pitch, boolean loop) {
        playSoundInternal(instanceName, soundName, volume, pitch, loop, new Vector3(x, y, z), null);
    }

    public void playSoundAttached(String instanceName, String soundName, String objectId, float volume, float pitch, boolean loop) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance == null) return;
        Vector3 initialPos = instance.transform.getTranslation(new Vector3());
        playSoundInternal(instanceName, soundName, volume, pitch, loop, initialPos, objectId);
    }

    public boolean prepareAudio(String soundName, String fileName, boolean asMusic) {
        if (soundName == null || soundName.isEmpty() || loadedAudioAssets.containsKey(soundName)) {
            return false;
        }
        try {
            File soundFile = ProjectManager.getInstance().getCurrentProject().getFile(fileName);
            if (soundFile != null && soundFile.exists()) {
                FileHandle fileHandle = Gdx.files.absolute(soundFile.getAbsolutePath());
                AudioAsset asset = new AudioAsset(fileHandle, asMusic);
                loadedAudioAssets.put(soundName, asset);
                return true;
            }
        } catch (Exception e) {
            Gdx.app.error("ThreeDManager_Audio", "Failed to load audio: " + fileName, e);
        }
        return false;
    }


    private void playSoundInternal(String instanceName, String soundName, float volume, float pitch, boolean loop, Vector3 position, String attachedToId) {
        if (instanceName == null || instanceName.isEmpty()) return;
        stopSound(instanceName);

        AudioAsset asset = loadedAudioAssets.get(soundName);
        if (asset == null) return;

        PlayableAudio playableAudio;
        if (asset.music != null) {

            Music newMusicInstance = Gdx.audio.newMusic(Gdx.files.absolute(asset.filePath));
            playableAudio = new MusicWrapper(newMusicInstance, volume, pitch, loop);
        } else {
            playableAudio = new SoundWrapper(asset.sound, volume, pitch, loop);
        }

        playableAudio.setInstanceName(instanceName);
        playableAudio.setPosition(position);
        playableAudio.setAttachedObjectId(attachedToId);

        playableAudio.play();
        active3DSounds.add(playableAudio);
    }


    public void stopSound(String instanceName) {
        Iterator<PlayableAudio> iterator = active3DSounds.iterator();
        while (iterator.hasNext()) {
            PlayableAudio audio = iterator.next();
            if (audio.getInstanceName().equals(instanceName)) {
                audio.stop();
                audio.dispose();
                iterator.remove();
                break;
            }
        }
    }


    public void setGlobalSoundVolume(float volume) {
        this.globalSoundVolume = Math.max(0, Math.min(1, volume));
    }

    public Array<String> getAnimationNames(String objectId) {
        ModelInstance instance = sceneObjects.get(objectId);
        Array<String> names = new Array<>();
        if (instance != null && instance.animations.size > 0) {
            for (Animation anim : instance.animations) {
                names.add(anim.id);
            }
        }
        return names;
    }

    public void setSkybox(String panoramicTexturePath) {
        if (!realisticMode) {
            Gdx.app.error("Skybox", "Skybox can only be enabled in realistic rendering mode.");
            return;
        }

        if (panoramicConverter == null) {
            Gdx.app.error("Skybox", "PanoramicConverter is not initialized yet. Deferring task.");

            Gdx.app.postRunnable(() -> setSkybox(panoramicTexturePath));
            return;
        }

        if (panoramicTexturePath == null || panoramicTexturePath.isEmpty()) {
            if (skybox != null) {
                sceneManager.setSkyBox(null);
                skybox.dispose();
                skybox = null;
            }
            if (skyboxCubemap != null) {
                skyboxCubemap.dispose();
                skyboxCubemap = null;
            }
            updateProceduralIBL();
            Gdx.app.log("Skybox", "Skybox cleared. Reverted to procedural IBL.");
            return;
        }

        try {
            FileHandle textureFile = Gdx.files.absolute(panoramicTexturePath);
            if (!textureFile.exists()) {
                Gdx.app.error("Skybox", "Skybox texture not found: " + panoramicTexturePath);
                return;
            }

            Texture panoramicTexture = new Texture(textureFile);

            if (skyboxCubemap != null) skyboxCubemap.dispose();
            if (skybox != null) skybox.dispose();

            skyboxCubemap = panoramicConverter.convert(panoramicTexture, 1024);
            skybox = new net.mgsx.gltf.scene3d.scene.SceneSkybox(skyboxCubemap);
            sceneManager.setSkyBox(skybox);


            updateIBLFromCubemap(skyboxCubemap);

            panoramicTexture.dispose();

            Gdx.app.log("Skybox", "Skybox and its Image-Based Lighting set successfully.");

        } catch (Exception e) {
            Gdx.app.error("Skybox", "Failed to set skybox", e);
        }
    }

    private void updateProceduralIBL() {
        if (diffuseCubemap != null) diffuseCubemap.dispose();
        if (specularCubemap != null) specularCubemap.dispose();

        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(pbrLight);

        diffuseCubemap = iblBuilder.buildIrradianceMap(256);
        specularCubemap = iblBuilder.buildRadianceMap(10);

        iblBuilder.dispose();

        sceneManager.environment.set(new PBRCubemapAttribute(PBRCubemapAttribute.DiffuseEnv, diffuseCubemap));
        sceneManager.environment.set(new PBRCubemapAttribute(PBRCubemapAttribute.SpecularEnv, specularCubemap));
    }



    private void updateIBLFromCubemap(Cubemap sourceCubemap) {
        if (iblBuilderCompat == null) {
            Gdx.app.error("IBL", "IBLBuilderCompat not initialized yet. Deferring IBL update.");

            Gdx.app.postRunnable(() -> updateIBLFromCubemap(sourceCubemap));
            return;
        }

        if (diffuseCubemap != null) diffuseCubemap.dispose();
        if (specularCubemap != null) specularCubemap.dispose();


        diffuseCubemap = iblBuilderCompat.buildIrradianceMap(sourceCubemap, 32);



        specularCubemap = iblBuilderCompat.buildRadianceMap(sourceCubemap, 128, 5);



        sceneManager.environment.set(new PBRCubemapAttribute(PBRCubemapAttribute.DiffuseEnv, diffuseCubemap));
        sceneManager.environment.set(new PBRCubemapAttribute(PBRCubemapAttribute.SpecularEnv, specularCubemap));
        Gdx.app.log("IBL", "Image-Based Lighting recalculated from skybox texture via Compat builder.");
    }





    public void enableRealisticRendering(boolean enabled) {
        this.realisticMode = enabled;
    }


    public void setVelocity(String objectId, float vx, float vy, float vz) {
        btRigidBody body = physicsBodies.get(objectId);
        if (body != null && body.getInvMass() > 0) {
            body.activate();
            body.setLinearVelocity(new Vector3(vx, vy, vz));
        }
    }

    public void setGravity(float x, float y, float z) {
        dynamicsWorld.setGravity(new Vector3(x, y, z));
    }

    public void renderShadowsOnly() {
        camera.update();
        if (realisticMode) {
            sceneManager.renderShadows();
        } else {

        }
    }

    public void renderColorsOnly() {
        camera.update();


        if (realisticMode) {
            if (skyColor.a != 0) {
                Gdx.gl.glClearColor(skyColor.r, skyColor.g, skyColor.b, skyColor.a);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
            }
            sceneManager.renderMirror();
            sceneManager.renderTransmission();
            sceneManager.renderColors();
        } else {
            Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            if (skyColor.a != 0) {
                Gdx.gl.glClearColor(skyColor.r, skyColor.g, skyColor.b, skyColor.a);
                Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT | com.badlogic.gdx.graphics.GL20.GL_DEPTH_BUFFER_BIT);
            }

            modelBatch.begin(camera);

            if (customShaderProvider != null) {
                int shadowMapTextureUnit = 5;
                shadowFBO.getColorBufferTexture().bind(shadowMapTextureUnit);
                setShaderUniform("shadowMap", shadowMapTextureUnit);
                customUniforms.put("u_lightSpaceMatrix", lightSpaceMatrix);
                customUniforms.put("u_shadowMapSize", new Vector2(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE));
            }

            for (Map.Entry<String, ModelInstance> entry : sceneObjects.entrySet()) {
                if (!inactiveRenderObjects.contains(entry.getKey())) {
                    modelBatch.render(entry.getValue(), environment);
                }
            }
            modelBatch.end();
        }

        if (particleSystemInitialized && !activeParticleEffects.isEmpty()) {

            particleModelBatch.begin(camera);
            particleModelBatch.render(particleSystem);
            particleModelBatch.end();


            if (!effectsNormal.isEmpty()) {
                Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
                Gdx.gl.glDepthMask(true);
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                batchNormal.setCamera(camera);
                batchNormal.begin();
                for (ParticleEffect effect : effectsNormal) effect.draw();
                batchNormal.end();
            }

            if (!effectsAdditive.isEmpty()) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                Gdx.gl.glDepthMask(false);

                batchAdditive.setCamera(camera);
                batchAdditive.begin();
                for (ParticleEffect effect : effectsAdditive) effect.draw();
                batchAdditive.end();

                Gdx.gl.glDepthMask(true);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }

        if (debugEnabled) {
            debugDrawer.begin(camera);
            dynamicsWorld.debugDrawWorld();
            debugDrawer.end();
        }
    }

    private SpriteBatch blitBatch;
    private OrthographicCamera blitCamera;
    private TextureRegion sceneFboRegion;
    private static final float HDR_EMULATION_FACTOR = 1.0f / 16.0f;

    public void render() {
        try {
            float delta = Gdx.graphics.getDeltaTime();

            updateParticles(delta);

            if (postprocessingEnabled) {
                if (vfxManager != null) {
                    vfxManager.update(delta);
                }

                renderShadowsOnly();

                sceneFbo2.begin();
                renderColorsOnly();
                sceneFbo2.end();

                if (currentConfig.hasEffect(PostProcessingData.EyeAdaptation.class)) {
                    PostProcessingData.EyeAdaptation settings = (PostProcessingData.EyeAdaptation) currentConfig.effects.stream()
                            .filter(e -> e instanceof PostProcessingData.EyeAdaptation).findFirst().orElse(null);
                    if (settings != null) {
                        eyeAdaptationManager.update(delta, sceneFbo2.getColorBufferTexture(), settings);
                        if (exposureEffect != null) {
                            exposureEffect.setExposure(eyeAdaptationManager.getCurrentExposure());
                        }
                    }
                }


                vfxManager.cleanUpBuffers();
                vfxManager.beginInputCapture();


                blitCamera.update();
                blitBatch.setProjectionMatrix(blitCamera.combined);
                blitBatch.begin();

                sceneFboRegion.flip(false, !sceneFboRegion.isFlipY());
                sceneFboRegion.flip(false, false);

                blitBatch.draw(sceneFboRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                blitBatch.end();

                vfxManager.endInputCapture();
                vfxManager.applyEffects();
                vfxManager.renderToScreen();
            } else {
                camera.update();

                if (realisticMode) {
                    if (skyColor.a != 0) {
                        Gdx.gl.glClearColor(skyColor.r, skyColor.g, skyColor.b, skyColor.a);
                        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT | com.badlogic.gdx.graphics.GL20.GL_DEPTH_BUFFER_BIT);
                    }

                    sceneManager.render();

                } else {
                    Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

                    if (skyColor.a != 0) {
                        Gdx.gl.glClearColor(skyColor.r, skyColor.g, skyColor.b, skyColor.a);
                        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT | com.badlogic.gdx.graphics.GL20.GL_DEPTH_BUFFER_BIT);
                    }

                    modelBatch.begin(camera);

                    if (customShaderProvider != null) {
                        int shadowMapTextureUnit = 5;
                        shadowFBO.getColorBufferTexture().bind(shadowMapTextureUnit);
                        setShaderUniform("shadowMap", shadowMapTextureUnit);
                        customUniforms.put("u_lightSpaceMatrix", lightSpaceMatrix);
                        customUniforms.put("u_shadowMapSize", new Vector2(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE));
                    }

                    for (Map.Entry<String, ModelInstance> entry : sceneObjects.entrySet()) {
                        if (!inactiveRenderObjects.contains(entry.getKey())) {
                            modelBatch.render(entry.getValue(), environment);
                        }
                    }
                    modelBatch.end();
                }

                if (particleSystemInitialized && !activeParticleEffects.isEmpty()) {
                    particleModelBatch.begin(camera);
                    particleModelBatch.render(particleSystem);
                    particleModelBatch.end();

                    if (!effectsNormal.isEmpty()) {
                        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
                        Gdx.gl.glDepthMask(true);
                        Gdx.gl.glEnable(GL20.GL_BLEND);
                        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                        batchNormal.setCamera(camera);
                        batchNormal.begin();
                        for (ParticleEffect effect : effectsNormal) effect.draw();
                        batchNormal.end();
                    }

                    if (!effectsAdditive.isEmpty()) {
                        Gdx.gl.glEnable(GL20.GL_BLEND);
                        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                        Gdx.gl.glDepthMask(false);

                        batchAdditive.setCamera(camera);
                        batchAdditive.begin();
                        for (ParticleEffect effect : effectsAdditive) effect.draw();
                        batchAdditive.end();

                        Gdx.gl.glDepthMask(true);
                        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                    }
                }

                if (debugEnabled) {
                    debugDrawer.begin(camera);
                    dynamicsWorld.debugDrawWorld();
                    debugDrawer.end();
                }
            }
        } catch (Exception e) {
            Log.e("ThreeDManager", "FATAL 3D RENDER ERROR", e);
            e.printStackTrace();

            try { modelBatch.end(); } catch (Exception ignored) {}
            try { particleModelBatch.end(); } catch (Exception ignored) {}
            try { if (sceneFbo2 != null) sceneFbo2.end(); } catch (Exception ignored) {}
        }
    }

    public void createTestFire() {

        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.ORANGE);
        pixmap.fill();
        Texture tex = new Texture(pixmap);


        BillboardParticleBatch testBatch = new BillboardParticleBatch();
        testBatch.setCamera(camera);
        particleSystem.add(testBatch);


        BillboardRenderer renderer = new BillboardRenderer(testBatch);


        com.badlogic.gdx.graphics.g3d.particles.ParticleController controller =
                new com.badlogic.gdx.graphics.g3d.particles.ParticleController(
                        "TestFire",
                        new RegularEmitter(),
                        renderer,
                        new RegionInfluencer.Single(new TextureRegion(tex))
                );


        RegularEmitter emitter = (RegularEmitter) controller.emitter;
        emitter.getEmission().setHigh(100);
        emitter.getLife().setHigh(2000);


        DynamicsInfluencer dynamics = new DynamicsInfluencer();
        com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier.BrownianAcceleration acc =
                new com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier.BrownianAcceleration();
        acc.strengthValue.setHigh(5f);
        dynamics.velocities.add(acc);
        controller.influencers.add(dynamics);


        ScaleInfluencer scale = new ScaleInfluencer();
        scale.value.setHigh(2f);
        controller.influencers.add(scale);


        ColorInfluencer.Single col = new ColorInfluencer.Single();
        col.colorValue.setColors(new float[]{1, 0.5f, 0});
        col.alphaValue.setHigh(1);
        controller.influencers.add(col);


        controller.init();
        controller.start();
        controller.setTransform(new Matrix4().setToTranslation(0, 5, 0));

        ParticleEffect effect = new ParticleEffect();
        effect.getControllers().add(controller);


        activeParticleEffects.put("TEST_FIRE", effect);
    }

    private <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }


    public void setObjectColor(String objectId, float r, float g, float b) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance == null) return;

        for (com.badlogic.gdx.graphics.g3d.Material material : instance.materials) {
            material.remove(com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute.Diffuse);
            material.set(ColorAttribute.createDiffuse(r, g, b, 1.0f));
        }
    }


    public void setObjectTexture(String objectId, String texturePath) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance == null || texturePath == null || texturePath.isEmpty()) return;

        Texture texture = loadedTextures.get(texturePath);

        if (texture == null) {
            try {
                FileHandle textureFile = Gdx.files.absolute(texturePath);
                if (textureFile.exists()) {
                    texture = new com.badlogic.gdx.graphics.Texture(textureFile);
                    texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
                    loadedTextures.put(texturePath, texture);
                } else {
                    Gdx.app.error("3DManager", "Texture file not found: " + texturePath);
                    return;
                }
            } catch (Exception e) {
                Gdx.app.error("3DManager", "Could not load texture: " + texturePath, e);
                return;
            }
        }


        for (com.badlogic.gdx.graphics.g3d.Material material : instance.materials) {
            material.set(com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute.createDiffuse(texture));

            material.set(new com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute(
                    com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                    com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA
            ));
        }
    }


    private void createMeshPhysicsBody(String objectId, ModelInstance instance) {

        com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray vertexArray =
                new com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray(instance.model.meshParts);

        btBvhTriangleMeshShape meshShape = new btBvhTriangleMeshShape(vertexArray, true);

        Vector3 scale = new Vector3();
        instance.transform.getScale(scale);

        meshShape.setLocalScaling(scale);

        com.badlogic.gdx.math.Matrix4 bodyTransform = new com.badlogic.gdx.math.Matrix4();
        Vector3 position = new Vector3();
        instance.transform.getTranslation(position);
        Quaternion rotation = new Quaternion();
        instance.transform.getRotation(rotation);
        bodyTransform.set(position, rotation);

        btMotionState motionState = new btDefaultMotionState(bodyTransform);
        float mass = 0f;
        Vector3 localInertia = new Vector3(0, 0, 0);

        btRigidBody.btRigidBodyConstructionInfo bodyInfo =
                new btRigidBody.btRigidBodyConstructionInfo(mass, motionState, meshShape, localInertia);
        btRigidBody body = new btRigidBody(bodyInfo);

        dynamicsWorld.addRigidBody(body);
        physicsBodies.put(objectId, body);

        bodyInfo.dispose();
    }




    public boolean checkCollision(String objectId1, String objectId2) {
        btRigidBody body1 = physicsBodies.get(objectId1);
        btRigidBody body2 = physicsBodies.get(objectId2);

        if (body1 == null || body2 == null) {
            return false;
        }

        collisionCallback.collided = false;
        dynamicsWorld.contactPairTest(body1, body2, collisionCallback);

        return collisionCallback.collided;
    }


    public void castRay(String rayName, Vector3 from, Vector3 direction) {
        tmpPos.set(from).add(direction.x * camera.far, direction.y * camera.far, direction.z * camera.far);

        com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback callback =
                new com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback(from, tmpPos);

        dynamicsWorld.rayTest(from, tmpPos, callback);

        RayCastResult result = rayCastResults.get(rayName);
        if (result == null) {
            result = new RayCastResult();
            rayCastResults.put(rayName, result);
        }
        if (callback.hasHit()) {
            result.hasHit = true;

            callback.getHitPointWorld(result.hitPoint);
            callback.getHitNormalWorld(result.hitNormal);

            com.badlogic.gdx.physics.bullet.collision.btCollisionObject hitObject = callback.getCollisionObject();
            for (Map.Entry<String, btRigidBody> entry : physicsBodies.entrySet()) {
                if (entry.getValue().equals(hitObject)) {
                    result.hitObjectId = entry.getKey();
                    break;
                }
            }
            result.hitDistance = from.dst(result.hitPoint);
        } else {
            result.hasHit = false;
            result.hitObjectId = "";
            result.hitDistance = -1.0f;
        }

        rayCastResults.put(rayName, result);
        callback.dispose();
    }

    public boolean getRayDidHit(String rayName) {
        RayCastResult result = rayCastResults.get(rayName);
        return result != null && result.hasHit;
    }

    public float getRayHitPointX(String rayName) {
        RayCastResult result = rayCastResults.get(rayName);
        return (result != null && result.hasHit) ? result.hitPoint.x : 0f;
    }

    public float getRayHitPointY(String rayName) {
        RayCastResult result = rayCastResults.get(rayName);
        return (result != null && result.hasHit) ? result.hitPoint.y : 0f;
    }

    public float getRayHitPointZ(String rayName) {
        RayCastResult result = rayCastResults.get(rayName);
        return (result != null && result.hasHit) ? result.hitPoint.z : 0f;
    }

    public float getRayHitNormalX(String rayName) {
        RayCastResult result = rayCastResults.get(rayName);
        return (result != null && result.hasHit) ? result.hitNormal.x : 0f;
    }

    public float getRayHitNormalY(String rayName) {
        RayCastResult result = rayCastResults.get(rayName);
        return (result != null && result.hasHit) ? result.hitNormal.y : 0f;
    }

    public float getRayHitNormalZ(String rayName) {
        RayCastResult result = rayCastResults.get(rayName);
        return (result != null && result.hasHit) ? result.hitNormal.z : 0f;
    }


    public float getRaycastDistance(String rayName) {
        RayCastResult result = rayCastResults.get(rayName);
        return (result != null) ? result.hitDistance : -1.0f;
    }

    public void setRotation(String objectId, Quaternion newRotation) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance == null) return;

        Vector3 position = new Vector3();
        instance.transform.getTranslation(position);
        Vector3 scale = new Vector3();
        instance.transform.getScale(scale);
        instance.transform.set(position, newRotation, scale);

        btRigidBody body = physicsBodies.get(objectId);
        if (body != null && !editorMode) {
            Matrix4 transform = body.getWorldTransform();
            transform.getTranslation(position); 
            transform.set(position, newRotation);
            body.setWorldTransform(transform);
            body.getMotionState().setWorldTransform(transform);
            body.activate();
        }
    }


    public String getRaycastHitObjectId(String rayName) {
        RayCastResult result = rayCastResults.get(rayName);
        return (result != null) ? result.hitObjectId : "";
    }




    public boolean createObject(String objectId, String modelPath) {
        if (sceneObjects.containsKey(objectId)) return false;

        try {
            FileHandle fileHandle;
            if (modelPath.startsWith("/")) {
                fileHandle = Gdx.files.absolute(modelPath);
            } else {
                fileHandle = Gdx.files.internal("models/" + modelPath);
            }

            if (!fileHandle.exists()) {
                Gdx.app.error("3DManager", "Model file not found: " + fileHandle.path());
                return false;
            }

            String lowerCasePath = modelPath.toLowerCase();

            if (lowerCasePath.endsWith(".glb") || lowerCasePath.endsWith(".gltf")) {
                net.mgsx.gltf.scene3d.scene.SceneAsset sceneAsset;

                if (lowerCasePath.endsWith(".glb")) {
                    sceneAsset = new net.mgsx.gltf.loaders.glb.GLBLoader().load(fileHandle, true);
                } else {
                    sceneAsset = new net.mgsx.gltf.loaders.gltf.GLTFLoader().load(fileHandle, true);
                }

                if (sceneAsset != null) {

                    net.mgsx.gltf.scene3d.scene.Scene scene = new net.mgsx.gltf.scene3d.scene.Scene(sceneAsset.scene);

                    sceneManager.addScene(scene);
                    sceneObjects.put(objectId, scene.modelInstance);

                    gltfObjectIds.add(objectId);

                    if (scene.modelInstance.animations.size > 0) {
                        com.badlogic.gdx.graphics.g3d.utils.AnimationController controller = new com.badlogic.gdx.graphics.g3d.utils.AnimationController(scene.modelInstance);
                        animationControllers.put(objectId, controller);
                    }
                    return true;
                }


            } else {
                Model model = loadedModels.get(modelPath);

                if (model == null) {
                    try {
                        FileHandle modelFileHandle;

                        if (modelPath.startsWith("/")) {
                            modelFileHandle = Gdx.files.absolute(modelPath);
                        } else {
                            modelFileHandle = Gdx.files.internal("models/" + modelPath);
                        }

                        if (!modelFileHandle.exists()) {
                            Gdx.app.error("3DManager", "Model file does not exist: " + modelPath);
                            return false;
                        }

                        FileHandle patchedModelHandle = ModelPathProcessor.process(modelFileHandle);

                        Gdx.app.log("3DManager", "--- Verification before loading model ---");
                        FileHandle textureToVerify = Gdx.files.local("Lowpoly_Laptop_Nor_2.jpg");
                        Gdx.app.log("3DManager", "Verifying texture path: " + textureToVerify.path());
                        Gdx.app.log("3DManager", "Does texture exist at path? -> " + textureToVerify.exists());
                        Gdx.app.log("3DManager", "--- Verification finished ---");

                        FileHandleResolver resolver = new FileHandleResolver() {
                            @Override
                            public FileHandle resolve(String fileName) {
                                return patchedModelHandle.parent().child(fileName);
                            }
                        };

                        ObjLoader loader = new ObjLoader(resolver);

                        model = loader.loadModel(patchedModelHandle, true);
                        loadedModels.put(modelPath, model);
                    } catch (Exception e) {
                        Gdx.app.error("3DManager", "Could not load model: " + modelPath, e);
                        return false;
                    }
                }

                ModelInstance instance = new ModelInstance(model);
                sceneObjects.put(objectId, instance);

                if (realisticMode) {
                    net.mgsx.gltf.scene3d.attributes.PBRColorAttribute baseColor =
                            net.mgsx.gltf.scene3d.attributes.PBRColorAttribute.createBaseColorFactor(com.badlogic.gdx.graphics.Color.WHITE);

                    for (Material mat : instance.materials) {
                        mat.set(baseColor);
                        mat.set(net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute.createMetallic(0.0f));
                        mat.set(net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute.createRoughness(1.0f));
                    }

                    net.mgsx.gltf.scene3d.scene.Scene pbrScene = new net.mgsx.gltf.scene3d.scene.Scene(instance);
                    sceneManager.addScene(pbrScene);
                }

                if (hiddenSpawnIds.contains(objectId)) { hiddenSpawnIds.remove(objectId); setObjectVisibility(objectId, false); }

                return true;
            }



        } catch (Exception e) {
            Gdx.app.error("3DManager_PBR", "Failed to load GLTF model: " + modelPath, e);
            e.printStackTrace();
            return false;
        }

        return false;
    }


    public void playAnimation(String objectId, String animationName, int loops, float speed, float transitionTime) {
        AnimationController controller = animationControllers.get(objectId);
        if (controller != null) {
            controller.animate(animationName, loops, speed, new AnimationController.AnimationListener() {
                @Override
                public void onLoop(AnimationController.AnimationDesc animation) {

                }

                @Override
                public void onEnd(AnimationController.AnimationDesc animation) {

                }
            }, transitionTime);
        }
    }


    public void stopAnimation(String objectId) {
        AnimationController controller = animationControllers.get(objectId);
        if (controller != null) {
            controller.setAnimation(null);
        }
    }

    private float currentShadowSize = 100f;
    private int currentShadowResolution = 2048;


    private void setupDefaultLighting() {
        if (pbrLight != null) {
            sceneManager.environment.remove(pbrLight);
            pbrLight = null;
        }

        net.mgsx.gltf.scene3d.lights.DirectionalShadowLight shadowLight = new net.mgsx.gltf.scene3d.lights.DirectionalShadowLight(2048, 2048);
        shadowLight.direction.set(1, -1.5f, 1).nor();
        shadowLight.color.set(com.badlogic.gdx.graphics.Color.WHITE);
        shadowLight.intensity = 5.0f;
        shadowLight.getCamera().far = 1000f;

        sceneManager.environment.add(shadowLight);
        pbrLight = shadowLight;

        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        updateProceduralIBL();
    }


    public void setRealisticSunLight(float dirX, float dirY, float dirZ, float intensity) {
        
        if (pbrLight instanceof net.mgsx.gltf.scene3d.lights.DirectionalShadowLight) {
            net.mgsx.gltf.scene3d.lights.DirectionalShadowLight sun = (net.mgsx.gltf.scene3d.lights.DirectionalShadowLight) pbrLight;

            sun.direction.set(dirX, dirY, dirZ).nor();
            sun.intensity = intensity;

            //Gdx.app.log("ThreeDManager", "Sun light updated and IBL recalculated.");
        } else {
            Gdx.app.error("ThreeDManager", "setRealisticSunLight called, but no DirectionalShadowLight was found!");
        }
    }

    
    
    public void setSunLightColor(float r, float g, float b) {
        if (pbrLight != null) {
            pbrLight.color.set(r, g, b, 1);
        }
    }

    public void applyForce(String objectId, float forceX, float forceY, float forceZ) {
        btRigidBody body = physicsBodies.get(objectId);
        if (body != null) {
            body.activate();
            body.applyCentralForce(new Vector3(forceX, forceY, forceZ));
        }
    }


    public void setPhysicsState(String objectId, PhysicsState state, PhysicsShape shape, float mass) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance == null) return;

        removePhysicsBody(objectId);
        if (state == PhysicsState.NONE) return;

        float bodyMass = (state == PhysicsState.DYNAMIC) ? mass : 0f;
        if (state == PhysicsState.DYNAMIC && mass <= 0) {
            Gdx.app.error("3DManager", "Dynamic body must have mass > 0.");
            return;
        }

        if (state == PhysicsState.MESH_STATIC) {
            if (gltfObjectIds.contains(objectId)) {
                createGltfMeshPhysicsBody(objectId, instance);
            } else {
                createMeshPhysicsBody(objectId, instance);
            }
        } else {
            createPrimitivePhysicsBody(objectId, instance, shape, bodyMass);
        }
    }

    
    public void setPhysicsState(String objectId, PhysicsState state, float mass) {
        setPhysicsState(objectId, state, PhysicsShape.BOX, mass);
    }


    private void createPrimitivePhysicsBody(String objectId, ModelInstance instance, PhysicsShape shapeType, float mass) {
        BoundingBox bbox = new BoundingBox();
        instance.calculateBoundingBox(bbox);
        Vector3 dimensions = bbox.getDimensions(new Vector3());
        
        Vector3 center = bbox.getCenter(new Vector3());
        btCollisionShape shape;
        switch (shapeType) {
            case SPHERE:
                float radius = Math.max(dimensions.x, Math.max(dimensions.y, dimensions.z)) / 2f;
                shape = new btSphereShape(radius);
                break;
            case CAPSULE:
                float capsuleRadius = Math.max(dimensions.x, dimensions.z) / 2f;
                float capsuleHeight = dimensions.y - (2 * capsuleRadius);
                shape = new btCapsuleShape(capsuleRadius, Math.max(0, capsuleHeight));
                break;
            case BOX:
            default:
                shape = new btBoxShape(dimensions.cpy().scl(0.5f));
                break;
        }

        btCompoundShape compoundShape = new btCompoundShape();
        
        Matrix4 shapeOffsetTransform = new Matrix4().setToTranslation(center);
        compoundShape.addChildShape(shapeOffsetTransform, shape);

        Vector3 scale = instance.transform.getScale(new Vector3());
        compoundShape.setLocalScaling(scale);

        Vector3 localInertia = new Vector3();
        if (mass > 0f) {
            compoundShape.calculateLocalInertia(mass, localInertia);
        }

        Matrix4 bodyTransform = new Matrix4();
        bodyTransform.set(instance.transform.getRotation(new Quaternion()));
        bodyTransform.setTranslation(instance.transform.getTranslation(new Vector3()));

        btMotionState motionState = new btDefaultMotionState(bodyTransform);
        btRigidBody.btRigidBodyConstructionInfo bodyInfo =
                new btRigidBody.btRigidBodyConstructionInfo(mass, motionState, compoundShape, localInertia);
        btRigidBody body = new btRigidBody(bodyInfo);

        if (mass > 0) {
            body.setAngularFactor(1f);

            body.setDamping(0.5f, 0.5f);
        }

        dynamicsWorld.addRigidBody(body);
        physicsBodies.put(objectId, body);

        Array<Disposable> disposables = new Array<>();
        disposables.add(shape);
        disposables.add(compoundShape);
        body.userData = disposables;
        physicsResources.put(objectId, disposables);

        bodyInfo.dispose();
    }

    


    private void createGltfMeshPhysicsBody(String objectId, ModelInstance instance) {
        Gdx.app.log("PhysicsDebug", "============================================================");
        Gdx.app.log("PhysicsDebug", "=== STARTING MESH BODY CREATION (BAKING METHOD) for object: '" + objectId + "'");
        Gdx.app.log("PhysicsDebug", "============================================================");

        instance.calculateTransforms();

        btCompoundShape compoundShape = new btCompoundShape();
        Array<Disposable> disposables = new Array<>();

        if (instance.nodes.size == 0) {
            Gdx.app.error("PhysicsDebug", "FATAL: ModelInstance has NO nodes!");
        } else {
            Gdx.app.log("PhysicsDebug", "Model has " + instance.nodes.size + " root nodes. Starting recursion...");

            addPartsToCompoundShapeRecursive(instance.nodes, instance.transform, compoundShape, disposables, "  ");
        }

        int childCount = compoundShape.getNumChildShapes();
        Gdx.app.log("PhysicsDebug", "RECURSION FINISHED. Total child shapes in CompoundShape: " + childCount);

        if (childCount == 0) {
            Gdx.app.error("PhysicsDebug", "FATAL: CompoundShape is EMPTY!");
            compoundShape.dispose();
            for (Disposable d : disposables) d.dispose();
            Gdx.app.log("PhysicsDebug", "=================== CREATION FAILED ===================");
            return;
        }

        Matrix4 bodyTransform = new Matrix4().idt();

        Gdx.app.log("PhysicsDebug", "RigidBody initial transform is IDENTITY because geometry is pre-transformed.");

        btMotionState motionState = new btDefaultMotionState(bodyTransform);
        btRigidBody.btRigidBodyConstructionInfo bodyInfo =
                new btRigidBody.btRigidBodyConstructionInfo(0f, motionState, compoundShape, new Vector3(0, 0, 0));
        btRigidBody body = new btRigidBody(bodyInfo);

        body.setCollisionFlags(body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_STATIC_OBJECT);
        body.setActivationState(Collision.DISABLE_DEACTIVATION);

        dynamicsWorld.addRigidBody(body);
        dynamicsWorld.updateSingleAabb(body);

        physicsBodies.put(objectId, body);
        disposables.add(compoundShape);
        body.userData = disposables;
        physicsResources.put(objectId, disposables);

        Gdx.app.log("PhysicsDebug", "SUCCESS: RigidBody added to dynamicsWorld. AABB was manually updated.");
        Gdx.app.log("PhysicsDebug", "=================== CREATION FINISHED for: '" + objectId + "' ===================");

        bodyInfo.dispose();
    }


    public void createEditorProxy(String ownerId) {
        if (editorProxies.containsKey(ownerId) || lightProxyModel == null) return;
        ModelInstance proxyInstance = new ModelInstance(lightProxyModel);
        editorProxies.put(ownerId, proxyInstance);
    }


    public void removeEditorProxy(String ownerId) {
        editorProxies.remove(ownerId);
    }


    public void updateEditorProxyPosition(String ownerId, Vector3 position) {
        ModelInstance proxy = editorProxies.get(ownerId);
        if (proxy != null) {
            proxy.transform.setTranslation(position);
        }
    }


    public Map<String, ModelInstance> getEditorProxies() {
        return editorProxies;
    }


    
    private void addPartsToCompoundShapeRecursive(Iterable<Node> nodes, Matrix4 rootTransform, btCompoundShape compoundShape, Array<Disposable> disposables, String indent) {
        for (Node node : nodes) {
            Gdx.app.log("PhysicsDebug", indent + "-> Processing Node: '" + node.id + "'");

            Matrix4 finalTransform = new Matrix4(rootTransform).mul(node.globalTransform);

            if (node.parts.size > 0) {
                for (NodePart nodePart : node.parts) {
                    MeshPart originalMeshPart = nodePart.meshPart;
                    if (originalMeshPart != null && originalMeshPart.mesh != null && originalMeshPart.mesh.getNumVertices() > 0) {
                        Mesh bakedMesh = originalMeshPart.mesh.copy(true);

                        bakedMesh.transform(finalTransform);
                        disposables.add(bakedMesh); 

                        MeshPart bakedMeshPart = new MeshPart("baked_part_" + node.id, bakedMesh, 0, bakedMesh.getNumIndices(), GL20.GL_TRIANGLES);

                        btTriangleIndexVertexArray vertexArray = new btTriangleIndexVertexArray(bakedMeshPart);
                        btBvhTriangleMeshShape meshShape = new btBvhTriangleMeshShape(vertexArray, true);
                        meshShape.setMargin(0.04f);

                        disposables.add(vertexArray);
                        disposables.add(meshShape);

                        compoundShape.addChildShape(new Matrix4(), meshShape); 
                        Gdx.app.log("PhysicsDebug", indent + "  SUCCESS: Baked and added child shape.");
                    }
                }
            }

            if (node.hasChildren()) {
                addPartsToCompoundShapeRecursive(node.getChildren(), rootTransform, compoundShape, disposables, indent + "  ");
            }
        }
    }

    Boolean objectExists(String id) {
        return sceneObjects.containsKey(id);
    }


    public Vector3 getVelocity(String objectId) {
        btRigidBody body = physicsBodies.get(objectId);
        if (body != null && body.getInvMass() > 0) {
            return body.getLinearVelocity();
        }
        return Vector3.Zero;
    }


    public Vector3 getCameraRotation() {
        Quaternion q = new Quaternion();

        q.setFromMatrix(camera.view);

        q.conjugate();

        return new Vector3(q.getPitch(), q.getYaw(), q.getRoll());
    }

    public ModelInstance getModelInstance(String objectId) {
        return sceneObjects.get(objectId);
    }


    public boolean createSphere(String objectId) {
        if (sceneObjects.containsKey(objectId)) return false;

        final String SPHERE_MODEL_KEY = "__PRIMITIVE_SPHERE__";
        Model sphereModel = loadedModels.get(SPHERE_MODEL_KEY);

        if (sphereModel == null) {
            Material pbrMaterial = new Material(
                    PBRColorAttribute.createBaseColorFactor(Color.WHITE)
            );

            final long attributes = VertexAttributes.Usage.Position |
                    VertexAttributes.Usage.Normal |
                    VertexAttributes.Usage.TextureCoordinates |
                    VertexAttributes.Usage.Tangent;

            sphereModel = modelBuilder.createSphere(50f, 50f, 50f, 16, 16,
                    pbrMaterial,
                    attributes);
            loadedModels.put(SPHERE_MODEL_KEY, sphereModel);
        }

        ModelInstance instance = new ModelInstance(sphereModel);
        sceneObjects.put(objectId, instance);

        if (realisticMode && sceneManager != null) {
            sceneManager.addScene(new net.mgsx.gltf.scene3d.scene.Scene(instance));
        }

        if (hiddenSpawnIds.contains(objectId)) { hiddenSpawnIds.remove(objectId); setObjectVisibility(objectId, false); }

        return true;
    }

    private void createPhysicsBody(String objectId, ModelInstance instance, float mass) {
        BoundingBox bbox = new BoundingBox();
        instance.model.calculateBoundingBox(bbox);
        Vector3 dimensions = new Vector3();
        bbox.getDimensions(dimensions);
        Vector3 center = new Vector3();
        bbox.getCenter(center);

        btBoxShape boxShape = new btBoxShape(dimensions.scl(0.5f));

        btCompoundShape compoundShape = new btCompoundShape();
        com.badlogic.gdx.math.Matrix4 shapeOffsetTransform = new com.badlogic.gdx.math.Matrix4();
        shapeOffsetTransform.setToTranslation(center);
        compoundShape.addChildShape(shapeOffsetTransform, boxShape);

        Vector3 scale = new Vector3();
        instance.transform.getScale(scale);
        compoundShape.setLocalScaling(scale);

        Vector3 localInertia = new Vector3();
        if (mass > 0f) {
            compoundShape.calculateLocalInertia(mass, localInertia);
        }

        com.badlogic.gdx.math.Matrix4 bodyTransform = new com.badlogic.gdx.math.Matrix4();
        Vector3 position = new Vector3();
        instance.transform.getTranslation(position);
        Quaternion rotation = new Quaternion();
        instance.transform.getRotation(rotation);
        bodyTransform.set(position, rotation);

        btMotionState motionState = new btDefaultMotionState(bodyTransform);
        btRigidBody.btRigidBodyConstructionInfo bodyInfo =
                new btRigidBody.btRigidBodyConstructionInfo(mass, motionState, compoundShape, localInertia);
        btRigidBody body = new btRigidBody(bodyInfo);

        dynamicsWorld.addRigidBody(body);
        physicsBodies.put(objectId, body);

        bodyInfo.dispose();
    }




    private final Map<String, com.badlogic.gdx.utils.Array<Disposable>> physicsResources = new HashMap<>();








    private void addNodePartsToCompoundShape(com.badlogic.gdx.graphics.g3d.model.Node node, btCompoundShape compoundShape) {
        if (node.parts.size > 0) {
            Gdx.app.log("PhysicsDebug", "Processing Node: " + node.id + " with " + node.parts.size + " parts.");

            com.badlogic.gdx.utils.Array<Disposable> disposables = new com.badlogic.gdx.utils.Array<>();

            for (com.badlogic.gdx.graphics.g3d.model.NodePart nodePart : node.parts) {
                com.badlogic.gdx.graphics.g3d.model.MeshPart meshPart = nodePart.meshPart;
                if (meshPart.size > 0) {
                    btTriangleIndexVertexArray vertexArray = new btTriangleIndexVertexArray(meshPart);
                    disposables.add(vertexArray);

                    btBvhTriangleMeshShape meshShape = new btBvhTriangleMeshShape(vertexArray, true);
                    disposables.add(meshShape);

                    compoundShape.addChildShape(node.globalTransform, meshShape);
                }
            }

            for (Disposable d : disposables) {
                d.dispose();
            }
        }


        for (com.badlogic.gdx.graphics.g3d.model.Node child : node.getChildren()) {
            addNodePartsToCompoundShape(child, compoundShape);
        }
    }


    void removePhysicsBody(String objectId) {
        btRigidBody body = physicsBodies.remove(objectId);
        if (body != null) {
            dynamicsWorld.removeRigidBody(body);

            com.badlogic.gdx.utils.Array<Disposable> resources = physicsResources.remove(objectId);
            if (resources != null) {
                for (Disposable d : resources) {
                    try {
                        d.dispose();
                    } catch (Exception ignored) {
                    }
                }
            }

            if (body.userData instanceof com.badlogic.gdx.utils.Array) {
                com.badlogic.gdx.utils.Array<Disposable> disposables = (com.badlogic.gdx.utils.Array<Disposable>) body.userData;
                for (Disposable d : disposables) {
                    try {
                        d.dispose();
                    } catch (Exception e) {
                        // None
                    }
                }
            }

            if (body.getMotionState() != null) body.getMotionState().dispose();
            if (body.getCollisionShape() != null) body.getCollisionShape().dispose();
            body.dispose();
        }
    }


    public boolean checkIntersection(String objectId1, String objectId2) {
        ModelInstance instance1 = sceneObjects.get(objectId1);
        ModelInstance instance2 = sceneObjects.get(objectId2);

        if (instance1 == null || instance2 == null) {
            return false;
        }

        instance1.calculateBoundingBox(bounds1);

        bounds1.mul(instance1.transform);

        instance2.calculateBoundingBox(bounds2);
        bounds2.mul(instance2.transform);

        return bounds1.intersects(bounds2);
    }

    public boolean removeObject(String objectId) {
        manager.removeGameObject(manager.findGameObject(objectId));
        ModelInstance instance = sceneObjects.remove(objectId);
        if (instance != null) {
            removePhysicsBody(objectId);
            animationControllers.remove(objectId);

            gltfObjectIds.remove(objectId);

            com.badlogic.gdx.utils.Array<com.badlogic.gdx.graphics.g3d.RenderableProvider> providers = sceneManager.getRenderableProviders();

            for (com.badlogic.gdx.graphics.g3d.RenderableProvider provider : providers) {

                if (provider instanceof net.mgsx.gltf.scene3d.scene.Scene) {
                    net.mgsx.gltf.scene3d.scene.Scene scene = (net.mgsx.gltf.scene3d.scene.Scene) provider;


                    if (scene.modelInstance == instance) {

                        sceneManager.removeScene(scene);

                        break;
                    }
                }
            }

            return true;
        }
        return false;
    }

    public boolean removeLight(String id) {
        DirectionalLight light = directionalLights.remove(id);
        return light != null;
    }

    public void setPosition(String objectId, float x, float y, float z) {
        if (manager != null && manager.findGameObject(objectId) != null) {
            GameObject go = manager.findGameObject(objectId);
            manager.setPosition(go, new Vector3(x, y, z));
        }


        ModelInstance instance = sceneObjects.get(objectId);
        if (instance == null) {
            return;
        }

        instance.transform.setTranslation(x, y, z);

        btRigidBody body = physicsBodies.get(objectId);
        if (body != null && !editorMode) {
            Matrix4 worldTransform = body.getWorldTransform();

            Quaternion rotation = worldTransform.getRotation(new Quaternion());
            worldTransform.set(new Vector3(x, y, z), rotation);

            body.setWorldTransform(worldTransform);
            body.getMotionState().setWorldTransform(worldTransform);

            body.activate();
        }
    }

    public void setRotation(String objectId, float yaw, float pitch, float roll) {
        Quaternion newRotation = new Quaternion().setEulerAngles(yaw, pitch, roll);
        if (manager != null && manager.findGameObject(objectId) != null) {
            GameObject go = manager.findGameObject(objectId);
            manager.setRotation(go, newRotation);
        }
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance == null) return;

        Vector3 position = new Vector3();
        instance.transform.getTranslation(position);
        Vector3 scale = new Vector3();
        instance.transform.getScale(scale);
        instance.transform.set(position, newRotation, scale);

        btRigidBody body = physicsBodies.get(objectId);
        if (body != null && !editorMode) {
            com.badlogic.gdx.math.Matrix4 transform = body.getWorldTransform();

            transform.getTranslation(position);

            transform.set(position, newRotation);

            body.setWorldTransform(transform);
            body.getMotionState().setWorldTransform(transform);
            body.activate();
        }
    }

    public Quaternion getRotationQuaternion(String objectId) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance != null) {
            Quaternion q = new Quaternion();
            instance.transform.getRotation(q, true);
            return q;
        }
        return null;
    }

    public void setCameraPosition(float x, float y, float z) {
        camera.position.set(x, y, z);
        camera.update();
    }

    public void cameraLookAt(float x, float y, float z) {
        camera.lookAt(x, y, z);
        camera.update();
    }

    public void objectLookAt(String id, float x, float y, float z) {
        ModelInstance instance = sceneObjects.get(id);
        if (instance == null) return;

        Vector3 position = new Vector3();
        instance.transform.getTranslation(position);
        Vector3 scale = new Vector3();
        instance.transform.getScale(scale);

        instance.transform.setToLookAt(new Vector3(x, y, z), Vector3.Y);
        Quaternion newRotation = new Quaternion();
        instance.transform.getRotation(newRotation);

        instance.transform.set(position, newRotation, scale);

        btRigidBody body = physicsBodies.get(id);
        if (body != null) {
            com.badlogic.gdx.math.Matrix4 transform = body.getWorldTransform();
            transform.getTranslation(position);
            transform.set(position, newRotation);

            body.setWorldTransform(transform);
            body.getMotionState().setWorldTransform(transform);
            body.activate();
        }
    }


    public void setCameraRotation(float yaw, float pitch, float roll) {
        Quaternion rotation = new Quaternion();
        rotation.setEulerAngles(yaw, pitch, roll);

        camera.direction.set(0, 0, -1);
        rotation.transform(camera.direction);
        camera.up.set(0, 1, 0);
        rotation.transform(camera.up);

        camera.update();
    }

    public void setAmbientLight(float r, float g, float b) {
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, r, g, b, 1f));
    }

    public void setDirectionalLight(String lightId, float r, float g, float b, float dirX, float dirY, float dirZ) {
        DirectionalLight light = directionalLights.get(lightId);
        if (light == null) {
            light = new DirectionalLight();
            directionalLights.put(lightId, light);
            environment.add(light);
        }
        light.set(r, g, b, dirX, dirY, dirZ);
    }

    public Camera getCamera() {
        return camera;
    }


    public void setScale(String objectId, float scaleX, float scaleY, float scaleZ) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance == null) return;

        Quaternion rotation = new Quaternion();
        instance.transform.getRotation(rotation);

        Vector3 position = new Vector3();
        instance.transform.getTranslation(position);
        instance.transform.set(position, rotation, new Vector3(scaleX, scaleY, scaleZ));

        btRigidBody body = physicsBodies.get(objectId);
        if (body != null && !editorMode) {
            float mass = (body.getInvMass() > 0f) ? 1f / body.getInvMass() : 0f;
            float friction = body.getFriction();
            float restitution = body.getRestitution();
            Vector3 velocity = body.getLinearVelocity();
            PhysicsState state = (mass > 0f) ? PhysicsState.DYNAMIC : PhysicsState.STATIC;

            setPhysicsState(objectId, state, mass);

            btRigidBody newBody = physicsBodies.get(objectId);
            if (newBody != null) {
                com.badlogic.gdx.math.Matrix4 transform = newBody.getWorldTransform();
                transform.getTranslation(position);
                transform.set(position, rotation);
                newBody.setWorldTransform(transform);
                newBody.getMotionState().setWorldTransform(transform);

                newBody.setFriction(friction);
                newBody.setRestitution(restitution);
                if (state == PhysicsState.DYNAMIC) {
                    newBody.setLinearVelocity(velocity);
                }
            }
        }
    }


    public void createGrid(float size, int divisions) {
        if (gridInstance != null) {
            sceneManager.removeScene(new net.mgsx.gltf.scene3d.scene.Scene(gridInstance));
            gridInstance.model.dispose();
        }

        com.badlogic.gdx.graphics.g3d.utils.ModelBuilder modelBuilder = new com.badlogic.gdx.graphics.g3d.utils.ModelBuilder();
        Model gridModel = modelBuilder.createLineGrid(divisions, divisions, size / divisions, size / divisions,
                new com.badlogic.gdx.graphics.g3d.Material(),
                com.badlogic.gdx.graphics.VertexAttributes.Usage.Position | com.badlogic.gdx.graphics.VertexAttributes.Usage.ColorUnpacked);

        gridInstance = new ModelInstance(gridModel);
        sceneManager.addScene(new net.mgsx.gltf.scene3d.scene.Scene(gridInstance));
    }


    public float getDeltaTime() {
        return Gdx.graphics.getDeltaTime();
    }

    public void setShadowsEnabled(boolean enabled) {
        if (sceneManager == null || pbrLight == null) return;

        boolean isShadowLight = pbrLight instanceof net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;

        if (enabled && !isShadowLight) {

            Color oldColor = pbrLight.color.cpy();
            Vector3 oldDir = pbrLight.direction.cpy();
            float oldIntensity = pbrLight.intensity;

            sceneManager.environment.remove(pbrLight);
            recreateShadowLight();


            if (pbrLight != null) {
                pbrLight.direction.set(oldDir);
                pbrLight.color.set(oldColor);
                pbrLight.intensity = oldIntensity;
            }
        }
        else if (!enabled && isShadowLight) {

            net.mgsx.gltf.scene3d.lights.DirectionalShadowLight shadowLight =
                    (net.mgsx.gltf.scene3d.lights.DirectionalShadowLight) pbrLight;

            net.mgsx.gltf.scene3d.lights.DirectionalLightEx simpleLight =
                    new net.mgsx.gltf.scene3d.lights.DirectionalLightEx();

            simpleLight.direction.set(shadowLight.direction);
            simpleLight.color.set(shadowLight.color);
            simpleLight.intensity = shadowLight.intensity;

            sceneManager.environment.remove(shadowLight);
            if (shadowLight instanceof Disposable) ((Disposable) shadowLight).dispose();

            sceneManager.environment.add(simpleLight);
            pbrLight = simpleLight;
        }
    }

    public boolean createCube(String objectId) {
        if (sceneObjects.containsKey(objectId)) return false;

        final String CUBE_MODEL_KEY = "__PRIMITIVE_CUBE__";
        Model cubeModel = loadedModels.get(CUBE_MODEL_KEY);

        if (cubeModel == null) {
            Material pbrMaterial = new Material(
                    PBRColorAttribute.createBaseColorFactor(Color.WHITE)
            );

            final long attributes = VertexAttributes.Usage.Position |
                    VertexAttributes.Usage.Normal |
                    VertexAttributes.Usage.TextureCoordinates |
                    VertexAttributes.Usage.Tangent;

            cubeModel = modelBuilder.createBox(50f, 50f, 50f,
                    pbrMaterial,
                    attributes);
            loadedModels.put(CUBE_MODEL_KEY, cubeModel);
        }

        ModelInstance instance = new ModelInstance(cubeModel);
        sceneObjects.put(objectId, instance);

        if (realisticMode && sceneManager != null) {
            sceneManager.addScene(new net.mgsx.gltf.scene3d.scene.Scene(instance));
        }

        if (hiddenSpawnIds.contains(objectId)) { hiddenSpawnIds.remove(objectId); setObjectVisibility(objectId, false); }

        return true;
    }

    public void applyPBRMaterial(String objectId, MaterialComponent materialData) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance == null || materialData == null) return;

        for (Material material : instance.materials) {
            PBRColorAttribute colorAttr = (PBRColorAttribute) material.get(PBRColorAttribute.BaseColorFactor);
            if (colorAttr != null) {
                colorAttr.color.set(materialData.baseColor);
            } else {
                material.set(PBRColorAttribute.createBaseColorFactor(materialData.baseColor));
            }

            if (materialData.baseColor.a < 1.0f) {
                material.set(new com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute(
                        GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA
                ));
            } else {
                material.remove(com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute.Type);
            }

            applyTextureOrColor(material, materialData.baseColorTexturePath,
                    PBRTextureAttribute.BaseColorTexture, PBRColorAttribute.BaseColorFactor, materialData.baseColor);

            material.set(PBRFloatAttribute.createMetallic(materialData.metallic));
            material.set(PBRFloatAttribute.createRoughness(materialData.roughness));

            applyTexture(material, materialData.metallicRoughnessTexturePath, PBRTextureAttribute.MetallicRoughnessTexture);

            applyTexture(material, materialData.normalTexturePath, PBRTextureAttribute.NormalTexture);

            applyTexture(material, materialData.occlusionTexturePath, PBRTextureAttribute.OcclusionTexture);

            applyTextureOrColor(material, materialData.emissiveTexturePath,
                    PBRTextureAttribute.EmissiveTexture, PBRColorAttribute.Emissive, materialData.emissiveColor);
        }
    }

    private void applyTextureOrColor(Material material, String texturePath, long textureType, long colorType, Color color) {
        if (texturePath != null && !texturePath.isEmpty()) {
            Texture texture = loadTexture(texturePath);
            if (texture != null) {
                material.remove(colorType);
                material.set(new PBRTextureAttribute(textureType, texture));
            }
        } else {
            material.remove(textureType);
            material.set(new PBRColorAttribute(colorType, color));
        }
    }

    private void applyTexture(Material material, String texturePath, long textureType) {
        if (texturePath != null && !texturePath.isEmpty()) {
            Texture texture = loadTexture(texturePath);
            if (texture != null) {
                material.set(new PBRTextureAttribute(textureType, texture));
            }
        } else {
            material.remove(textureType);
        }
    }

    private Texture loadTexture(String textureFileName) {
        Texture texture = loadedTextures.get(textureFileName);
        if (texture == null) {
            try {
                File textureFileHandle = org.catrobat.catroid.ProjectManager.getInstance().getCurrentProject().getFile(textureFileName);
                if (textureFileHandle != null && textureFileHandle.exists()) {
                    texture = new Texture(Gdx.files.absolute(textureFileHandle.getAbsolutePath()));
                    texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
                    loadedTextures.put(textureFileName, texture);
                } else {
                    Gdx.app.error("3DManager", "Texture file not found in project: " + textureFileName);
                    return null;
                }
            } catch (Exception e) {
                Gdx.app.error("3DManager", "Could not load texture: " + textureFileName, e);
                return null;
            }
        }
        return texture;
    }

    private void disposeManagedBatches() {
        for (BillboardParticleBatch batch : managedBatches.values()) {
            try {
                java.lang.reflect.Field poolField = BillboardParticleBatch.class.getDeclaredField("renderablePool");
                poolField.setAccessible(true);
                com.badlogic.gdx.utils.Pool<Renderable> renderablePool = (com.badlogic.gdx.utils.Pool<Renderable>) poolField.get(batch);
                Array<Renderable> allRenderables = new Array<>();
                renderablePool.freeAll(allRenderables);
                for(Renderable r : allRenderables){
                    if(r.meshPart.mesh != null) {
                        r.meshPart.mesh.dispose();
                    }
                }
                renderablePool.clear();

            } catch (Exception e) {
                Gdx.app.error("Dispose", "Failed to dispose meshes in BillboardParticleBatch via reflection", e);
            }
        }
        managedBatches.clear();
    }


    public Vector3 getPosition(String objectId) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance != null) {
            Vector3 position = new Vector3();
            instance.transform.getTranslation(position);
            return position;
        }
        return null;
    }


    public Vector3 getRotation(String objectId) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance != null) {
            Quaternion q = new Quaternion();
            instance.transform.getRotation(q);

            return new Vector3(q.getPitch(), q.getYaw(), q.getRoll());
        }
        return null;
    }


    public Vector3 getScale(String objectId) {
        ModelInstance instance = sceneObjects.get(objectId);
        if (instance != null) {
            Vector3 scale = new Vector3();
            instance.transform.getScale(scale);
            return scale;
        }
        return null;
    }

    public Float getDistance(String id1, String id2) {
        if (getPosition(id1) == null || getPosition(id2) == null) return -1f;
        return getPosition(id1).dst(getPosition(id2));
    }





    public void setFriction(String objectId, float friction) {
        btRigidBody body = physicsBodies.get(objectId);
        if (body != null) {
            if (friction > 9999) {
                body.setDamping(friction - 10000, friction - 10000);
            } else {
                body.setFriction(friction);
            }

            body.activate();
        }
    }


    public void setRestitution(String objectId, float restitution) {
        btRigidBody body = physicsBodies.get(objectId);
        if (body != null) {
            body.setRestitution(restitution);

            body.activate();
        }
    }


    public void setContinuousCollisionDetection(String objectId, boolean enabled) {
        btRigidBody body = physicsBodies.get(objectId);
        ModelInstance instance = sceneObjects.get(objectId);
        if (body == null || instance == null || body.isStaticObject()) {
            return;
        }
        if (enabled) {
            BoundingBox bbox = new BoundingBox();
            instance.model.calculateBoundingBox(bbox);
            Vector3 dimensions = new Vector3();
            bbox.getDimensions(dimensions);
            float minSize = Math.min(Math.min(dimensions.x, dimensions.y), dimensions.z);
            body.setCcdMotionThreshold(minSize * 0.5f);
            body.setCcdSweptSphereRadius(minSize * 0.5f);
        } else {
            body.setCcdMotionThreshold(0);
            body.setCcdSweptSphereRadius(0);
        }
    }


    public void setShaderCode(String vertexCode, String fragmentCode) {
        Gdx.app.postRunnable(() -> {
            Gdx.app.log("ShaderDebug", "--- setShaderCode CALLED ---");
            if (vertexCode == null || vertexCode.isEmpty() || fragmentCode == null || fragmentCode.isEmpty()) {
                resetSceneShader();
                return;
            }

            try {
                DefaultShader.Config config = new DefaultShader.Config(vertexCode, fragmentCode);

                ShaderProgram tempProgram = new ShaderProgram(vertexCode, fragmentCode);
                if (!tempProgram.isCompiled()) {
                    throw new Exception("Shader compilation failed: " + tempProgram.getLog());
                }
                tempProgram.dispose();

                if (customShaderProvider != null) customShaderProvider.dispose();
                if (modelBatch != null) modelBatch.dispose();

                customShaderProvider = new CustomShaderProvider(config, customUniforms);

                modelBatch = new ModelBatch(customShaderProvider);

                Gdx.app.log("ShaderDebug", "--- CustomShaderProvider and new ModelBatch CREATED successfully ---");
            } catch (Exception e) {
                Gdx.app.error("3DManager", "Shader setup failed: " + e.getMessage());
                resetSceneShader();
            }
        });
    }


    public void resetSceneShader() {
        Gdx.app.postRunnable(() -> {
            if (customShaderProvider != null) {
                customShaderProvider.dispose();
                customShaderProvider = null;
            }

            if (modelBatch != null) modelBatch.dispose();

            customUniforms.clear();

            modelBatch = new ModelBatch(defaultShaderProvider);


        });
    }


    public void setShaderUniform(String name, float value) {
        if (name != null && !name.isEmpty()) {
            customUniforms.put("u_" + name, value);
        }
    }


    public void setShaderUniform(String name, float x, float y, float z) {
        if (name != null && !name.isEmpty()) {
            customUniforms.put("u_" + name, new Vector3(x, y, z));
        }
    }

    public void setShaderUniform(String name, int value) {
        if (name != null && !name.isEmpty()) {
            customUniforms.put("u_" + name, value);
        }
    }


    public void setShaderUniform(String name, float x, float y) {
        if (name != null && !name.isEmpty()) {
            customUniforms.put("u_" + name, new Vector2(x, y));
        }
    }


    public void setShaderUniform(String name, float x, float y, float z, float w) {
        if (name != null && !name.isEmpty()) {
            customUniforms.put("u_" + name, new Vector4(x, y, z, w));
        }
    }

    public Vector3 getSunLightDirection() {
        if (pbrLight != null) {
            return pbrLight.direction;
        }
        return new Vector3(1, -1.5f, 1); 
    }

    public Color getSunLightColor() {
        if (pbrLight != null) {
            return pbrLight.color;
        }
        return Color.WHITE; 
    }

    public float getSunLightIntensity() {
        if (pbrLight != null) {
            return pbrLight.intensity;
        }
        return 0; 
    }

    public void setWorldTransform(String objectId, Matrix4 worldTransform) {
        if (LOG_THREED_MANAGER_DEBUG) {
            Vector3 receivedPos = new Vector3();
            Quaternion receivedRot = new Quaternion();
            worldTransform.getTranslation(receivedPos);
            worldTransform.getRotation(receivedRot, true);
            Log.d("TDM_DEBUG", "  >>> ThreeDManager.setWorldTransform() called for '" + objectId + "' <<<");
            Log.d("TDM_DEBUG", "    [SetWorldTrans] Received World Pos: " + receivedPos + ", Rot: " + receivedRot);
        }

        ModelInstance instance = sceneObjects.get(objectId);
        if (instance != null) {
            instance.transform.set(worldTransform);
            if (LOG_THREED_MANAGER_DEBUG) {
                Vector3 modelPos = new Vector3();
                instance.transform.getTranslation(modelPos);
                Log.d("TDM_DEBUG", "    [SetWorldTrans] ModelInstance '" + objectId + "' set to Pos: " + modelPos);
            }
        } else {
            if (LOG_THREED_MANAGER_DEBUG) Log.w("TDM_DEBUG", "    [SetWorldTrans] ModelInstance for '" + objectId + "' not found. Skipping model update.");
        }

        btRigidBody body = physicsBodies.get(objectId);
        if (body != null && !editorMode) {
            Matrix4 bodyTransform = body.getWorldTransform();
            Vector3 position = new Vector3();
            Quaternion rotation = new Quaternion();
            worldTransform.getTranslation(position);
            worldTransform.getRotation(rotation, true);

            bodyTransform.set(position, rotation);

            body.setWorldTransform(bodyTransform);
            if (body.getMotionState() != null) {
                body.getMotionState().setWorldTransform(bodyTransform);
            }
            body.activate();
            if (LOG_THREED_MANAGER_DEBUG) {
                Vector3 physicsPos = new Vector3();
                body.getWorldTransform().getTranslation(physicsPos);
                Log.d("TDM_DEBUG", "    [SetWorldTrans] Physics body '" + objectId + "' set to Pos: " + physicsPos);
            }
        } else {
            if (LOG_THREED_MANAGER_DEBUG && body == null) Log.w("TDM_DEBUG", "    [SetWorldTrans] Physics body for '" + objectId + "' not found. Skipping physics update.");
            else if (LOG_THREED_MANAGER_DEBUG && editorMode) Log.d("TDM_DEBUG", "    [SetWorldTrans] In editor mode, skipping physics update for '" + objectId + "'.");
        }
    }

    public void clearScene() {
        Array<String> objectIds = new Array<>();
        for (String id : sceneObjects.keySet()) {
            objectIds.add(id);
        }
        
        for (String id : objectIds) {
            removeObject(id);
        }
        sceneObjects.clear();

        Array<String> bodyIds = new Array<>();
        for (String id : physicsBodies.keySet()) {
            bodyIds.add(id);
        }
        for (String id : bodyIds) {
            removePhysicsBody(id);
        }
        physicsBodies.clear();
        physicsResources.clear();

        if (skybox != null) {
            skybox.dispose();
            skybox = null;
        }
        if (skyboxCubemap != null) {
            skyboxCubemap.dispose();
            skyboxCubemap = null;
        }
        if (diffuseCubemap != null) {
            diffuseCubemap.dispose();
            diffuseCubemap = null;
        }
        if (specularCubemap != null) {
            specularCubemap.dispose();
            specularCubemap = null;
        }
        if (gridInstance != null) {
            sceneManager.removeScene(new net.mgsx.gltf.scene3d.scene.Scene(gridInstance));
            gridInstance.model.dispose();
            gridInstance = null;
        }
        Array<String> constraintIds = new Array<>();
        for (String id : physicsConstraints.keySet()) {
            constraintIds.add(id);
        }
        for (String id : constraintIds) {
            removeConstraint(id);
        }

        if (particleSystemInitialized) {
            disposeManagedBatches();
            particleSystem = new ParticleSystem();
        }

        pointLights.clear();
        editorProxies.clear();
        spotLights.clear();
        directionalLights.clear();
        if (sceneManager != null) {
            sceneManager.environment.clear();

            try {
                setupDefaultLighting();
            } catch (Exception e) {
                Log.e("ThreeDManager", "Cloudn't setup lightning: " + e);
            }
        }

        animationControllers.clear();
        gltfObjectIds.clear();
        rayCastResults.clear();
    }


    @Override
    public void dispose() {
        clearScene();
        physicsConstraints.clear();

        if (particleProxyModel != null) particleProxyModel.dispose();
        if (modelBatch != null) modelBatch.dispose();
        if (shadowBatch != null) shadowBatch.dispose();
        if (defaultParticleTexture != null) defaultParticleTexture.dispose();
        if (particleModelBatch != null) particleModelBatch.dispose();
        if (particleSystemInitialized) disposeManagedBatches();
        if (blitBatch != null) blitBatch.dispose();
        if (iblBuilderCompat != null) iblBuilderCompat.dispose();
        if (eyeAdaptationManager != null) eyeAdaptationManager.dispose();
        if (exposureEffect != null) exposureEffect.dispose();
        if (tonemappingEffect != null) tonemappingEffect.dispose();
        if (defaultShaderProvider != null) defaultShaderProvider.dispose();
        if (customShaderProvider != null) customShaderProvider.dispose();
        if (depthShaderProvider != null) depthShaderProvider.dispose();
        if (sceneManager != null) sceneManager.dispose();
        if (lightProxyModel != null) lightProxyModel.dispose();
        if (cameraProxyModel != null) cameraProxyModel.dispose();
        if (wireframeBoxModel != null) wireframeBoxModel.dispose();
        if (wireframeSphereModel != null) wireframeSphereModel.dispose();
        if (wireframeCylinderModel != null) wireframeCylinderModel.dispose();
        if (wireframeBatch != null) wireframeBatch.dispose();
        if (brdfLUT != null) brdfLUT.dispose();
        if (panoramicConverter != null) panoramicConverter.dispose();
        if (debugDrawer != null) debugDrawer.dispose();
        if (dynamicsWorld != null) dynamicsWorld.dispose();
        if (solver != null) solver.dispose();
        if (broadphase != null) broadphase.dispose();
        if (dispatcher != null) dispatcher.dispose();
        if (collisionConfig != null) collisionConfig.dispose();
        if (collisionCallback != null) collisionCallback.dispose();
        if (shadowFBO != null) shadowFBO.dispose();
        if (sceneFbo2 != null) sceneFbo2.dispose();
        if (vfxManager != null) vfxManager.dispose();

        for (ParticleEffect eff : activeParticleEffects.values()) if (eff != null) eff.dispose();
        for (Model model : loadedModels.values()) if (model != null) model.dispose();
        for (Texture texture : loadedTextures.values()) if (texture != null) texture.dispose();
        for (PlayableAudio audio : active3DSounds) {
            if (audio == null) continue;
            audio.stop();
            audio.dispose();
        }
        for (AudioAsset asset : loadedAudioAssets.values()) {
            if (asset != null) asset.dispose();
        }

        managedBatches.clear();
        activeParticleEffects.clear();
        effectsNormal.clear();
        effectsAdditive.clear();
        loadedModels.clear();
        loadedTextures.clear();
        active3DSounds.clear();
        loadedAudioAssets.clear();

        modelBatch = null;
        dynamicsWorld = null;
        sceneManager = null;
    }

    private class CollisionCallback extends com.badlogic.gdx.physics.bullet.collision.ContactResultCallback {
        public boolean collided = false;

        @Override
        public float addSingleResult(com.badlogic.gdx.physics.bullet.collision.btManifoldPoint cp,
                                     com.badlogic.gdx.physics.bullet.collision.btCollisionObjectWrapper colObj0Wrap, int partId0, int index0,
                                     com.badlogic.gdx.physics.bullet.collision.btCollisionObjectWrapper colObj1Wrap, int partId1, int index1) {
            collided = true;
            return 0;
        }
    }


    private class NameAccumulatingContactCallback extends com.badlogic.gdx.physics.bullet.collision.ContactResultCallback {
        public StringBuilder namesBuilder = new StringBuilder();
        private String targetId;

        public void setTargetId(String targetId) {
            this.targetId = targetId;
            namesBuilder.setLength(0);
        }

        @Override
        public float addSingleResult(com.badlogic.gdx.physics.bullet.collision.btManifoldPoint cp,
                                     com.badlogic.gdx.physics.bullet.collision.btCollisionObjectWrapper colObj0Wrap, int partId0, int index0,
                                     com.badlogic.gdx.physics.bullet.collision.btCollisionObjectWrapper colObj1Wrap, int partId1, int index1) {


            btCollisionObject obj0 = colObj0Wrap.getCollisionObject();
            btCollisionObject obj1 = colObj1Wrap.getCollisionObject();


            String hitName = null;


            for (Map.Entry<String, btRigidBody> entry : physicsBodies.entrySet()) {
                if (entry.getValue().equals(obj0) && !entry.getKey().equals(targetId)) {
                    hitName = entry.getKey();
                    break;
                }
            }

            if (hitName == null) {
                for (Map.Entry<String, btRigidBody> entry : physicsBodies.entrySet()) {
                    if (entry.getValue().equals(obj1) && !entry.getKey().equals(targetId)) {
                        hitName = entry.getKey();
                        break;
                    }
                }
            }


            if (hitName != null && namesBuilder.indexOf(hitName) == -1) {
                if (namesBuilder.length() > 0) namesBuilder.append("\n");
                namesBuilder.append(hitName);
            }

            return 0;
        }
    }

    private NameAccumulatingContactCallback contactListCallback;


    public String getPhysicsCollisionsList(String objectId) {
        btRigidBody body = physicsBodies.get(objectId);
        if (body == null) return "";

        contactListCallback.setTargetId(objectId);

        dynamicsWorld.contactTest(body, contactListCallback);

        return contactListCallback.namesBuilder.toString();
    }

    public String getIntersectionCollisionsList(String objectId) {
        ModelInstance targetInstance = sceneObjects.get(objectId);
        if (targetInstance == null) return "";

        StringBuilder sb = new StringBuilder();


        targetInstance.calculateBoundingBox(bounds1);
        bounds1.mul(targetInstance.transform);

        for (Map.Entry<String, ModelInstance> entry : sceneObjects.entrySet()) {
            String otherId = entry.getKey();
            if (otherId.equals(objectId)) continue;

            ModelInstance otherInstance = entry.getValue();


            otherInstance.calculateBoundingBox(bounds2);
            bounds2.mul(otherInstance.transform);

            if (bounds1.intersects(bounds2)) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(otherId);
            }
        }
        return sb.toString();
    }
}