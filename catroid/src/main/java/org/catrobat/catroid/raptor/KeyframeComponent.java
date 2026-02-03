package org.catrobat.catroid.raptor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KeyframeComponent implements Component {
    public List<KeyframeData> keyframes = new ArrayList<>();

    public boolean autoStart = false;
    public boolean looping = false;

    public transient boolean isPlaying = false;
    public transient float currentTime = 0f;

    public KeyframeComponent() {}

    public void sortKeyframes() {
        synchronized (keyframes) {
            keyframes.sort(Comparator.comparingDouble(kf -> kf.time));
        }
    }

    public float getDuration() {
        if (keyframes.isEmpty()) {
            return 0f;
        }
        return keyframes.get(keyframes.size() - 1).time;
    }
}