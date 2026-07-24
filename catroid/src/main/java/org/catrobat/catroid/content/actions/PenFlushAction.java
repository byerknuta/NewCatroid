package org.catrobat.catroid.content.actions;

import android.util.Log;
import com.badlogic.gdx.scenes.scene2d.Action;
import org.catrobat.catroid.stage.PenActor;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.stage.StageListener;

public class PenFlushAction extends Action {
    @Override
    public boolean act(float delta) {
        try {
            StageListener listener = StageActivity.getActiveStageListener();
            if (listener == null) return true;

            PenActor penActor = listener.getPenActor();
            if (penActor != null) {
                penActor.flush();
            }
        } catch (Exception e) {
            Log.e("PenFlushAction", "Error flushing pen screen", e);
        }
        return true;
    }
}
