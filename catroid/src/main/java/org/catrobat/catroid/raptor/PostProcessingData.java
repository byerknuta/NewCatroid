package org.catrobat.catroid.raptor;

import com.badlogic.gdx.graphics.Color;

public abstract class PostProcessingData {
    public boolean isEnabled = true;
    public abstract String getType();


    public static class Bloom extends PostProcessingData {
        public float threshold = 0.8f;
        public float intensity = 1.2f;
        public int blurPasses = 2;
        public float blurAmount = 1.5f;
        public float size = 1f;

        @Override public String getType() { return "Bloom"; }
    }

    public static class Vignette extends PostProcessingData {
        public float intensity = 0.5f;
        public float saturation = 0.2f;

        @Override public String getType() { return "Vignette"; }
    }

    public static class Grain extends PostProcessingData {
        public float amount = 0.03f;

        @Override public String getType() { return "Film Grain"; }
    }

    public static class Levels extends PostProcessingData {
        public float contrast = 1.0f;
        public float saturation = 1.0f;
        public float gamma = 1.0f;

        @Override public String getType() { return "Levels"; }
    }

    public static class Chromatic extends PostProcessingData {
        public float maxDistortion = 4.0f;
        public float strength = 0.003f;

        @Override public String getType() { return "Chromatic Aberration"; }
    }

    public static class Fxaa extends PostProcessingData {
        @Override public String getType() { return "FXAA"; }
    }

    public static class RadialBlur extends PostProcessingData {
        public float strength = 0.2f;
        public int blurPasses = 2;
        public float size = 1f;

        @Override public String getType() { return "Radial Blur"; }
    }

    public static class OldTv extends PostProcessingData {
        public float strength = 0.5f;
        @Override public String getType() { return "Old TV"; }
    }

    public static class Gaussian extends PostProcessingData {
        public int passes = 1;
        public float amount = 1.0f;
        public float size = 1f;

        @Override public String getType() { return "Gaussian Blur"; }
    }

    public static class Zoom extends PostProcessingData {
        public float zoom = 0.25f;
        public float originX = 0.5f;
        public float originY = 0.5f;
        @Override public String getType() { return "Zoom Blur"; }
    }

    public static class Crt extends PostProcessingData {
        public float distortion = 0.3f;
        public float zoom = 1.0f;
        @Override public String getType() { return "CRT Monitor"; }
    }

    public static class Fisheye extends PostProcessingData {
        public float intensity = 0.4f;
        @Override public String getType() { return "Fisheye"; }
    }

    public static class Water extends PostProcessingData {
        public float speed = 1.0f;
        public float amount = 1.0f;
        @Override public String getType() { return "Water Distortion"; }
    }

    public static class MotionBlur extends PostProcessingData {
        public float blurOpacity = 0.5f;
        @Override public String getType() { return "Motion Blur"; }
    }

    public static class LensFlare extends PostProcessingData {
        public float intensity = 1.0f;
        public float threshold = 0.9f;
        public float dispersal = 0.4f;
        public float size = 2f;

        @Override public String getType() { return "Lens Flare"; }
    }

    public static class EyeAdaptation extends PostProcessingData {
        public float targetLuminance = 0.05f;
        public float speed = 2.0f;
        public float minExposure = 0.2f;
        public float maxExposure = 8.0f;

        @Override public String getType() { return "Eye Adaptation"; }
    }

    public static class ACES extends PostProcessingData {
        @Override public String getType() { return "ACES Tonemapping"; }
    }

    public static class RayTracing extends PostProcessingData {
        public int steps = 30;
        public float resolution = 1.0f;
        public float reflectivity = 0.6f;
        public float jitter = 0.1f;
        public float thickness = 1.0f;
        public float maxDistance = 50.0f;
        public float stride = 0.5f;
        public float edgeFade = 0.15f;

        @Override public String getType() { return "Ray Tracing (SSR)"; }
    }

    public static class SSAO extends PostProcessingData {
        public float radius = 1.0f;
        public float intensity = 1.0f;
        public float bias = 0.05f;

        @Override public String getType() { return "SSAO"; }
    }

    public static class HeightFog extends PostProcessingData {
        public float density = 0.01f;
        public float falloff = 0.05f;
        public float height = 0.0f;
        public Color color = new Color(0.5f, 0.6f, 0.7f, 1.0f);
        @Override public String getType() { return "Height Fog"; }
    }

    public static class DepthOfField extends PostProcessingData {
        public boolean autoFocus = false;
        public float autoFocusSpeed = 5.0f;

        public float focusDistance = 15.0f;
        public float focusRange = 5.0f;
        public float blurSize = 0.015f;
        public float transition = 10.0f;

        @Override public String getType() { return "Depth of Field"; }
    }

    public static class GodRays extends PostProcessingData {
        public float exposure = 0.3f;
        public float decay = 0.95f;
        public float density = 0.8f;
        public float weight = 0.6f;
        @Override public String getType() { return "God Rays"; }
    }

    public static class VolumetricFog extends PostProcessingData {
        public float density = 0.3f;
        public float scattering = 0.6f;
        public int steps = 30;
        public float maxDistance = 100.0f;
        @Override public String getType() { return "Volumetric Fog (3D)"; }
    }

    public static class Upscaler extends PostProcessingData {
        public float sharpness = 0.5f;
        @Override public String getType() { return "FSR Upscaler (CAS)"; }
    }

    public static class SSGI extends PostProcessingData {
        public float radius = 0.5f;
        public float intensity = 1.0f;
        public float bias = 0.05f;
        public float ssaoStrength = 1.0f;
        public float baseAlbedoR = 0.1f;
        public float baseAlbedoG = 0.1f;
        public float baseAlbedoB = 0.1f;
        public boolean flipDepth = true;

        @Override public String getType() { return "Global Illumination (SSGI)"; }
    }
}
