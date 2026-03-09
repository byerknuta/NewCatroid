package org.catrobat.catroid.ml;

import android.util.Log;

import androidx.annotation.NonNull;

public class MLBridge {
    public static native void nativeCreateRandomTensor(String name, int[] shape, boolean trainable);
    public static native void nativeCreateTensor(String name, int[] shape, float val, boolean trainable);

    public static native void nativeOp(String res, String a, String b, String op);
    public static native void nativeBackward(String lossVar);
    public static native void nativeStep(float lr);
    public static native void nativeSetTrainingMode(boolean isTrainMode);
    public static native void nativeSetTensor(String name, String data);
    public static native void nativeSetTensorByIndex(String name, int index, float value);

    public static native String nativeGetShape(String name);
    public static native float nativeGetTensorValueByIndex(String name, int index);
    public static native float nativeGetValueND(String name, String indices);
    public static native int nativeArgMax(String name);
    public static native int nativeGetTotalSize(String name);
    public static native void nativeLayerLinear(String layerName, String input, String output, int inFeatures, int outFeatures);
    public static native String nativeGetTensorAsString(String name);
    public static native boolean nativeLoadModel(String path);
    public static native void nativeSaveModel(String path);
    public static native void nativeStepAdam(float lr);

    public static native float[] nativeGetTensor(String name);

    public static native void nativeReshape(String name, int[] shape);

    //public static native float nativeGetTensorValueByIndex(String name, int index);

    public static void test() {
        nativeCreateTensor("x", new int[]{1, 1}, 2.0f, false);
        nativeCreateTensor("w", new int[]{1, 1}, 0.5f, true);
        nativeCreateTensor("target", new int[]{1, 1}, 10.0f, false);

        for (int i = 0; i < 50; i++) {
            nativeOp("y", "x", "w", "mul");            // y = 2 * w
            nativeOp("diff", "target", "y", "sub");     // diff = 10 - y
            nativeOp("loss", "diff", "diff", "mul");    // loss = (10 - y)^2 (MSE)

            nativeBackward("loss");
            nativeStep(0.01f);

            if (i % 10 == 0) {
                float weight = nativeGetTensor("w")[0];
                float loss = nativeGetTensor("loss")[0];
                Log.d("ML", "Step " + i + ": Weight = " + weight + ", Loss = " + loss);
            }
        }
    }
}