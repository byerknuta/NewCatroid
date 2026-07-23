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

    public static native void nativeResetEngine();

    public static native void nativeZeroGrad();
    public static native void nativeSetTensorArray(String name, float[] data);
    public static native void nativeSetOneHot(String name, int activeIndex);
    public static native int nativeSampleCategorical(String name);
    public static native void nativeCreateNormalTensor(String name, int[] shape, float mean, float stddev, boolean trainable);
    public static native void nativeSlice(String res, String input, int startCol, int endCol);
    public static native void nativeLayerEmbedding(String layerName, String input, String output, int vocabSize, int embDim);
    public static native void nativeLayerAttention(String layerName, String input, String output, int embedDim);
    public static native void nativeClipGrad(float maxNorm);
    public static native void nativeLayerConv2D(String layerName, String input, String output, int inChannels, int outChannels, int kernelSize, int stride);
    public static native void nativeMaxPool2D(String res, String input, int poolSize, int stride);
    public static native void nativeDropout(String res, String input, float p);
    public static native void nativeStepAdamW(float lr, float weightDecay);
    public static native void nativeLayerLstmCell(String layerName, String input, String hIn, String cIn, String hOut, String cOut, int inDim, int hiddenDim);
    public static native void nativeLayerGruCell(String layerName, String input, String hIn, String hOut, int inDim, int hiddenDim);

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
