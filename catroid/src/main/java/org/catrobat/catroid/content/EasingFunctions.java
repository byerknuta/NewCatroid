package org.catrobat.catroid.content;

public class EasingFunctions {

    public enum EasingType {
        LINEAR,
        QUAD_IN, QUAD_OUT, QUAD_IN_OUT,
        CUBIC_IN, CUBIC_OUT, CUBIC_IN_OUT,
        QUART_IN, QUART_OUT, QUART_IN_OUT,
        QUINT_IN, QUINT_OUT, QUINT_IN_OUT,
        SINE_IN, SINE_OUT, SINE_IN_OUT,
        EXPO_IN, EXPO_OUT, EXPO_IN_OUT,
        CIRC_IN, CIRC_OUT, CIRC_IN_OUT,
        ELASTIC_IN, ELASTIC_OUT, ELASTIC_IN_OUT,
        BACK_IN, BACK_OUT, BACK_IN_OUT,
        BOUNCE_IN, BOUNCE_OUT, BOUNCE_IN_OUT,
        SMOOTH_STEP, SMOOTHER_STEP
    }

    public static float calculate(EasingType type, float time, float duration, float start, float end) {
        if (duration <= 0) return end;
        if (time >= duration) return end;
        if (time <= 0) return start;

        float change = end - start;
        float t = time / duration;

        switch (type) {
            case LINEAR:
                return start + change * t;

            case QUAD_IN:
                return change * t * t + start;
            case QUAD_OUT:
                return -change * t * (t - 2) + start;
            case QUAD_IN_OUT:
                t *= 2;
                if (t < 1) return change / 2 * t * t + start;
                t--;
                return -change / 2 * (t * (t - 2) - 1) + start;

            case CUBIC_IN:
                return change * t * t * t + start;
            case CUBIC_OUT:
                t--;
                return change * (t * t * t + 1) + start;
            case CUBIC_IN_OUT:
                t *= 2;
                if (t < 1) return change / 2 * t * t * t + start;
                t -= 2;
                return change / 2 * (t * t * t + 2) + start;

            case QUART_IN:
                return change * t * t * t * t + start;
            case QUART_OUT:
                t--;
                return -change * (t * t * t * t - 1) + start;
            case QUART_IN_OUT:
                t *= 2;
                if (t < 1) return change / 2 * t * t * t * t + start;
                t -= 2;
                return -change / 2 * (t * t * t * t - 2) + start;

            case QUINT_IN:
                return change * t * t * t * t * t + start;
            case QUINT_OUT:
                t--;
                return change * (t * t * t * t * t + 1) + start;
            case QUINT_IN_OUT:
                t *= 2;
                if (t < 1) return change / 2 * t * t * t * t * t + start;
                t -= 2;
                return change / 2 * (t * t * t * t * t + 2) + start;

            case SINE_IN:
                return -change * (float) Math.cos(t * (Math.PI / 2)) + change + start;
            case SINE_OUT:
                return change * (float) Math.sin(t * (Math.PI / 2)) + start;
            case SINE_IN_OUT:
                return -change / 2 * (float) (Math.cos(Math.PI * t) - 1) + start;

            case EXPO_IN:
                return (t == 0) ? start : change * (float) Math.pow(2, 10 * (t - 1)) + start;
            case EXPO_OUT:
                return (t == 1) ? start + change : change * (float) (-Math.pow(2, -10 * t) + 1) + start;
            case EXPO_IN_OUT:
                if (t == 0) return start;
                if (t == 1) return start + change;
                t *= 2;
                if (t < 1) return change / 2 * (float) Math.pow(2, 10 * (t - 1)) + start;
                t--;
                return change / 2 * (float) (-Math.pow(2, -10 * t) + 2) + start;

            case CIRC_IN:
                return -change * ((float) Math.sqrt(1 - t * t) - 1) + start;
            case CIRC_OUT:
                t--;
                return change * (float) Math.sqrt(1 - t * t) + start;
            case CIRC_IN_OUT:
                t *= 2;
                if (t < 1) return -change / 2 * ((float) Math.sqrt(1 - t * t) - 1) + start;
                t -= 2;
                return change / 2 * ((float) Math.sqrt(1 - t * t) + 1) + start;

            case BACK_IN:
                float s = 1.70158f;
                return change * t * t * ((s + 1) * t - s) + start;
            case BACK_OUT:
                float s2 = 1.70158f;
                t--;
                return change * (t * t * ((s2 + 1) * t + s2) + 1) + start;
            case BACK_IN_OUT:
                float s3 = 1.70158f;
                t *= 2;
                s3 *= 1.525f;
                if (t < 1) return change / 2 * (t * t * ((s3 + 1) * t - s3)) + start;
                t -= 2;
                return change / 2 * (t * t * ((s3 + 1) * t + s3) + 2) + start;

            case ELASTIC_IN:
                if (t == 0) return start;
                if (t == 1) return start + change;
                float p = duration * 0.3f;
                float a = change;
                float s4 = p / 4;
                t--;
                return -(a * (float) Math.pow(2, 10 * t) * (float) Math.sin((t * duration - s4) * (2 * Math.PI) / p)) + start;
            case ELASTIC_OUT:
                if (t == 0) return start;
                if (t == 1) return start + change;
                float p2 = duration * 0.3f;
                float a2 = change;
                float s5 = p2 / 4;
                return (a2 * (float) Math.pow(2, -10 * t) * (float) Math.sin((t * duration - s5) * (2 * Math.PI) / p2) + change + start);
            case ELASTIC_IN_OUT:
                if (t == 0) return start;
                t *= 2;
                if (t == 2) return start + change;
                float p3 = duration * (0.3f * 1.5f);
                float a3 = change;
                float s6 = p3 / 4;
                if (t < 1) {
                    t--;
                    return -0.5f * (a3 * (float) Math.pow(2, 10 * t) * (float) Math.sin((t * duration - s6) * (2 * Math.PI) / p3)) + start;
                }
                t--;
                return a3 * (float) Math.pow(2, -10 * t) * (float) Math.sin((t * duration - s6) * (2 * Math.PI) / p3) * 0.5f + change + start;

            case BOUNCE_OUT:
                return start + calculateBounceOut(t, change);
            case BOUNCE_IN:
                return start + (change - calculateBounceOut(1 - t, change));
            case BOUNCE_IN_OUT:
                if (t < 0.5f) return start + (change - calculateBounceOut(1 - t * 2, change)) * 0.5f;
                return start + (calculateBounceOut(t * 2 - 1, change) * 0.5f + change * 0.5f);

            case SMOOTH_STEP:
                return start + change * (t * t * (3 - 2 * t));
            case SMOOTHER_STEP:
                return start + change * (t * t * t * (t * (t * 6 - 15) + 10));

            default:
                return start + change * t;
        }
    }

    private static float calculateBounceOut(float t, float c) {
        if (t < (1 / 2.75f)) {
            return c * (7.5625f * t * t);
        } else if (t < (2 / 2.75f)) {
            return c * (7.5625f * (t -= (1.5f / 2.75f)) * t + 0.75f);
        } else if (t < (2.5 / 2.75f)) {
            return c * (7.5625f * (t -= (2.25f / 2.75f)) * t + 0.9375f);
        } else {
            return c * (7.5625f * (t -= (2.625f / 2.75f)) * t + 0.984375f);
        }
    }
}