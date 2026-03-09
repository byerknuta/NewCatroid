package org.catrobat.catroid.content.actions;

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import org.catrobat.catroid.content.Scope;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.stage.StageActivity;

public class ThreedAttachObjectToBoneAction extends TemporalAction {
    public Scope scope;
    public Formula childId;
    public Formula parentId;
    public Formula boneName;
    public Formula offsetX;
    public Formula offsetY;
    public Formula offsetZ;

    @Override
    protected void update(float percent) {
        var listener = StageActivity.getActiveStageListener();
        if (listener == null || listener.sceneManager == null) return;

        try {
            String cId = childId.interpretString(scope);
            String pId = parentId.interpretString(scope);
            String bName = boneName.interpretString(scope);

            float ox = offsetX.interpretFloat(scope);
            float oy = offsetY.interpretFloat(scope);
            float oz = offsetZ.interpretFloat(scope);

            if (!cId.isEmpty() && !pId.isEmpty() && !bName.isEmpty()) {
                listener.sceneManager.attachObjectToBone(cId, pId, bName, ox, oy, oz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}