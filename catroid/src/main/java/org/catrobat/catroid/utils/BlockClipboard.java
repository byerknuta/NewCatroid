package org.catrobat.catroid.utils;

import org.catrobat.catroid.content.bricks.Brick;
import java.util.ArrayList;
import java.util.List;

public class BlockClipboard {
    private static BlockClipboard instance;
    private final List<List<Brick>> history = new ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 10;

    private BlockClipboard() {}

    public static synchronized BlockClipboard getInstance() {
        if (instance == null) {
            instance = new BlockClipboard();
        }
        return instance;
    }

    public void copy(List<Brick> bricks) {
        if (bricks == null || bricks.isEmpty()) return;
        List<Brick> clonedList = new ArrayList<>();
        for (Brick b : bricks) {
            try {
                clonedList.add(b.clone());
            } catch (CloneNotSupportedException ignored) {}
        }
        if (!clonedList.isEmpty()) {
            addToHistory(clonedList);
        }
    }

    private void addToHistory(List<Brick> bricks) {
        history.remove(bricks);
        history.add(0, bricks);
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(history.size() - 1);
        }
    }

    public List<List<Brick>> getHistory() {
        return history;
    }

    public List<Brick> getLatest() {
        if (history.isEmpty()) return null;
        return history.get(0);
    }
}
