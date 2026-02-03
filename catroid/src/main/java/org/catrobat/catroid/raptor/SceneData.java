package org.catrobat.catroid.raptor;

import com.badlogic.gdx.utils.Array;

public class SceneData {
    public Array<GameObject> gameObjects = new Array<>();

    public float skyR = 0.1f;
    public float skyG = 0.2f;
    public float skyB = 0.3f;

    public String skyboxPath = null;
    public float shadowSize = 100f;
    public float shadowResolution = 2048f;

    public float ambientIntensity = 1;

    public ThreeDManager.SceneSettings renderSettings = new ThreeDManager.SceneSettings();
}