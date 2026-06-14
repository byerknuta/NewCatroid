#ifdef GL_ES
precision highp float;
#endif

#define USE_NOISE

varying vec2 v_texCoords;
uniform sampler2D u_texture0;
uniform sampler2D u_depthTexture;
uniform sampler2D u_noiseTexture;

uniform mat4 u_projectionMatrix;
uniform mat4 u_invProjectionMatrix;
uniform mat4 u_viewMatrix;
uniform float u_farPlane;

uniform float u_radius;
uniform float u_intensity;
uniform float u_bias;
uniform float u_ssaoStrength;
uniform vec3 u_baseAlbedo;
uniform float u_flipDepth;

uniform vec2 u_noiseScale;
uniform vec3 u_kernel[12];

vec2 getDepthUV(vec2 uv) {
if (u_flipDepth > 0.5) {
return vec2(uv.x, 1.0 - uv.y);
}
return uv;
}

float getDepth(vec2 uv) {
vec2 data = texture2D(u_depthTexture, getDepthUV(uv)).rg;
return (data.x + data.y / 255.0) * u_farPlane;
}

vec3 getNormal(vec2 uv) {
vec2 p = texture2D(u_depthTexture, getDepthUV(uv)).ba * 2.0 - 1.0;
vec3 v = vec3(p.xy, 1.0 - abs(p.x) - abs(p.y));
if (v.z < 0.0) v.xy = (1.0 - abs(v.yx)) * vec2(p.x >= 0.0 ? 1.0 : -1.0, p.y >= 0.0 ? 1.0 : -1.0);
return normalize(mat3(u_viewMatrix) * v);
}

vec3 getViewPos(vec2 uv) {
float z = getDepth(uv);
vec4 ndc = vec4(uv * 2.0 - 1.0, 1.0, 1.0);
vec4 farRay = u_invProjectionMatrix * ndc;
return (farRay.xyz / farRay.w) * (z / u_farPlane);
}

void main() {
    vec4 baseColor = texture2D(u_texture0, v_texCoords);
    float originDepth = getDepth(v_texCoords);

    if (originDepth >= u_farPlane * 0.95) {
        gl_FragColor = baseColor;
        return;
    }

    vec3 vPos = getViewPos(v_texCoords);
    vec3 vNorm = getNormal(v_texCoords);

#ifdef USE_NOISE
    vec2 noise = texture2D(u_noiseTexture, v_texCoords * u_noiseScale).rg * 2.0 - 1.0;
    vec3 randomVec = normalize(vec3(noise, 0.0));
#else
    vec3 randomVec = vec3(0.0, 1.0, 0.0);
#endif

    vec3 tangent = normalize(randomVec - vNorm * dot(randomVec, vNorm));
    vec3 bitangent = cross(vNorm, tangent);
    mat3 TBN = mat3(tangent, bitangent, vNorm);

    vec3 indirectLight = vec3(0.0);
    float occlusion = 0.0;
    const int SAMPLES = 8;

    for (int i = 0; i < SAMPLES; i++) {
        vec3 samplePos = vPos + TBN * u_kernel[i] * u_radius;

        vec4 offset = u_projectionMatrix * vec4(samplePos, 1.0);
        vec2 sampleUV = (offset.xy / offset.w) * 0.5 + 0.5;

        float inScreen = step(0.0, sampleUV.x) * step(sampleUV.x, 1.0) * step(0.0, sampleUV.y) * step(sampleUV.y, 1.0);

        float sampleDepth = getDepth(sampleUV);
        vec3 sampleNorm = getNormal(sampleUV);

        vec3 sampleViewPos = getViewPos(sampleUV);
        vec3 L = sampleViewPos - vPos;
        float dist = length(L);

        if (dist < 0.001) {
            continue;
        }
        vec3 L_normalized = L / dist;

        float geoDiff = -samplePos.z - sampleDepth;
        float occWeight = step(u_bias, geoDiff) * (1.0 - smoothstep(u_bias, u_radius, geoDiff));
        occlusion += occWeight * max(0.0, dot(vNorm, L_normalized)) * inScreen;

        float cosTheta = max(0.0, dot(vNorm, L_normalized));
        float cosThetaSample = max(0.0, dot(sampleNorm, -L_normalized));
        float atten = 1.0 / (1.0 + dist * dist);

        float depthDiff = abs(originDepth - sampleDepth);
        float rangeCheck = smoothstep(u_radius, 0.0, depthDiff);

        float bounceWeight = cosTheta * cosThetaSample * atten * rangeCheck * (1.0 - occWeight) * inScreen;
        vec3 bounceColor = texture2D(u_texture0, sampleUV).rgb;

        indirectLight += bounceColor * bounceWeight;
    }

    occlusion /= float(SAMPLES);
    indirectLight /= float(SAMPLES);

    float ssaoFactor = clamp(1.0 - (occlusion * u_ssaoStrength), 0.1, 1.0);

    vec3 albedo = max(baseColor.rgb, u_baseAlbedo);

    vec3 finalColor = (baseColor.rgb * ssaoFactor) + (albedo * indirectLight * u_intensity);

    gl_FragColor = vec4(finalColor, baseColor.a);
}
