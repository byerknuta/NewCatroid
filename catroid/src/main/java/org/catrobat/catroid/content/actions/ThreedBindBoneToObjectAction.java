package org.catrobat.catroid.content.actions;

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import org.catrobat.catroid.content.Scope;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.stage.StageActivity;

public class ThreedBindBoneToObjectAction extends TemporalAction {
    public Scope scope;
    public Formula boneName;
    public Formula modelId;
    public Formula targetId;

    @Override
    protected void update(float percent) {
        var listener = StageActivity.getActiveStageListener();
        if (listener == null) return;
        var threeDManager = listener.getThreeDManager();
        if (threeDManager == null) return;

        try {
            String bName = boneName.interpretString(scope);
            String mId = modelId.interpretString(scope);
            String tId = targetId.interpretString(scope);

            if (!bName.isEmpty() && !mId.isEmpty()) {
                threeDManager.bindBoneToObject(mId, bName, tId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}