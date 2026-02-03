package org.catrobat.catroid.raptor;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import org.catrobat.catroid.content.EasingFunctions;

public class KeyframeData {
    public float time = 0f; // Время в секундах от начала анимации
    public Vector3 position = new Vector3();
    public Quaternion rotation = new Quaternion();
    public Vector3 scale = new Vector3(1, 1, 1);

    // Тип перехода к СЛЕДУЮЩЕМУ кадру
    public EasingFunctions.EasingType easingToNext = EasingFunctions.EasingType.LINEAR;

    public KeyframeData() {}

    // Конструктор копирования
    public KeyframeData(KeyframeData other) {
        this.time = other.time;
        this.position.set(other.position);
        this.rotation.set(other.rotation);
        this.scale.set(other.scale);
        this.easingToNext = other.easingToNext;
    }
}