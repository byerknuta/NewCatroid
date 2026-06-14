package org.catrobat.catroid.utils;

import android.util.Log;
import org.catrobat.catroid.content.Script;

public class PerformanceTracker {
    private static long totalPhysicsTime = 0;
    private static long totalLogicTime = 0;
    private static long totalRenderTime = 0;
    private static long frames = 0;

    public static long formulaEvaluations = 0;
    public static long blocksExecuted = 0;

    public static long activeThreads = 0;
    public static long totalBlockTimeNs = 0;

    private static long lastLogTime = System.currentTimeMillis();

    public static void recordFrame(long physicsNs, long logicNs, long renderNs) {
        totalPhysicsTime += physicsNs;
        totalLogicTime += logicNs;
        totalRenderTime += renderNs;
        frames++;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLogTime >= 1000) {
            printStats();
            reset();
            lastLogTime = currentTime;
        }
    }

    public static void logHeavyScript(Script script, long durationNs) {
        double ms = durationNs / 1_000_000.0;
        String scriptName = (script != null) ? script.getClass().getSimpleName() : "Unknown Script";
        Log.w("CAT_PROFILER_HEAVY", "🔥 HEAVY SCRIPT DETECTED: " + scriptName + " took " + String.format("%.2f", ms) + " ms in a single frame!");
    }

    private static void printStats() {
        if (frames == 0) return;

        double avgPhysics = (totalPhysicsTime / (double) frames) / 1_000_000.0;
        double avgLogic = (totalLogicTime / (double) frames) / 1_000_000.0;
        double avgRender = (totalRenderTime / (double) frames) / 1_000_000.0;
        double avgBlock = (totalBlockTimeNs / (double) frames) / 1_000_000.0;
        long avgThreads = activeThreads / frames;

        Log.i("CAT_PROFILER", String.format(
                "FPS: %d | Threads: %d | Logic: %.2fms (Blocks alone: %.2fms) | Render: %.2fms | Physics: %.2fms | Formulas: %d | Blocks: %d",
                frames, avgThreads, avgLogic, avgBlock, avgRender, avgPhysics, formulaEvaluations, blocksExecuted
        ));
    }

    private static void reset() {
        totalPhysicsTime = 0;
        totalLogicTime = 0;
        totalRenderTime = 0;
        frames = 0;
        formulaEvaluations = 0;
        blocksExecuted = 0;
        activeThreads = 0;
        totalBlockTimeNs = 0;
    }
}
