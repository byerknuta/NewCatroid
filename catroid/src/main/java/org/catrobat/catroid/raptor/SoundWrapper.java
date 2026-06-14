package org.catrobat.catroid.raptor;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector3;

public class SoundWrapper implements PlayableAudio {
    private final Sound sound;
    private long instanceId = -1;
    private String instanceName;
    private float baseVolume;
    private final float pitch;
    private final boolean loop;

    private Vector3 position = new Vector3();
    private String attachedToObjectId;
    private float maxDistance = 250f;

    public SoundWrapper(Sound sound, float volume, float pitch, boolean loop) {
        this.sound = sound;
        this.baseVolume = volume;
        this.pitch = pitch;
        this.loop = loop;
    }

    @Override
    public void play() {
        if (loop) {
            instanceId = sound.loop(baseVolume, pitch, 0);
        } else {
            instanceId = sound.play(baseVolume, pitch, 0);
        }
    }

    @Override public void stop() { if (instanceId != -1) sound.stop(instanceId); }
    @Override public void dispose() {  }
    @Override public boolean isPlaying() { return instanceId != -1; }

    @Override public void setInstanceName(String name) { this.instanceName = name; }
    @Override public String getInstanceName() { return instanceName; }

    @Override public void setPosition(Vector3 pos) { this.position.set(pos); }
    @Override public Vector3 getPosition() { return position; }

    @Override public void setAttachedObjectId(String id) { this.attachedToObjectId = id; }
    @Override public String getAttachedObjectId() { return attachedToObjectId; }

    @Override
    public void update3D(float volume, float pan) {
        if (instanceId != -1) {
            sound.setVolume(instanceId, volume * baseVolume);
            sound.setPan(instanceId, pan, volume * baseVolume);
        }
    }

    @Override
    public void setVolume(float volume) {
        baseVolume = volume;
    }

    @Override
    public void setPitch(float pitch) {
        sound.setPitch(instanceId, pitch);
    }

    @Override
    public void setBaseVolume(float volume) {
        this.baseVolume = volume;
    }

    @Override
    public float getBaseVolume() {
        return baseVolume;
    }

    @Override
    public void setMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
    }

    @Override
    public float getMaxDistance() {
        return this.maxDistance;
    }
}
