package org.catrobat.catroid.formulaeditor;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.danvexteam.lunoscript_annotations.LunoClass;

import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Scene;
import org.catrobat.catroid.content.Scope;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.UserDefinedScript;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;

@LunoClass
public final class UserDataWrapper {

    public static UserVariable getUserVariable(String name, Scope scope) {
        if (scope == null) return null;

        Sprite sprite = scope.getSprite();
        if (sprite != null) {
            UserVariable uv = sprite.getUserVariable(name);
            if (uv != null) return uv;
        }

        Project project = scope.getProject();
        if (project != null) {
            UserVariable uv = project.getUserVariable(name);
            if (uv != null) return uv;

            return project.getMultiplayerVariable(name);
        }
        return null;
    }

    public static UserList getUserList(String name, Scope scope) {
        if (scope == null) return null;

        Sprite sprite = scope.getSprite();
        if (sprite != null) {
            UserList ul = sprite.getUserList(name);
            if (ul != null) return ul;
        }

        Project project = scope.getProject();
        if (project != null) {
            return project.getUserList(name);
        }
        return null;
    }

    public static UserData getUserDefinedBrickInput(String value, SequenceAction sequence) {
        if (sequence instanceof ScriptSequenceAction) {
            Script script = ((ScriptSequenceAction) sequence).getScript();
            if (script instanceof UserDefinedScript) {
                return ((UserDefinedScript) script).getUserDefinedBrickInput(value);
            }
        }
        return null;
    }

    public static void resetAllUserData(Project project) {
        project.resetUserData();
        for (Scene scene : project.getSceneList()) {
            for (Sprite sprite : scene.getSpriteList()) {
                sprite.resetUserData();
            }
        }
    }

    private UserDataWrapper() {
        throw new AssertionError("No.");
    }
}
