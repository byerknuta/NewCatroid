package org.catrobat.catroid.stage.event;

import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Scene;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;

public class EventManager {
    public static boolean projectHasScriptOfType(Project project, Class<? extends Script> scriptClass) {
        if (project == null || scriptClass == null) {
            return false;
        }
        for (Scene scene : project.getSceneList()) {
            for (Sprite sprite : scene.getSpriteList()) {
                for (Script script : sprite.getScriptList()) {
                    if (scriptClass.isInstance(script)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
