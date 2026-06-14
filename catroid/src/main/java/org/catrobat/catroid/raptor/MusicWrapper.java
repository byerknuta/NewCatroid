package org.catrobat.catroid.raptor;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector3;

public class MusicWrapper implements PlayableAudio {
    private final Music music;
    private String instanceName;
    private final boolean loop;

    private Vector3 position = new Vector3();
    private String attachedToObjectId;
    private float baseVolume = 1.0f;
    private float maxDistance = 250f;

    public MusicWrapper(Music music, float volume, float pitch, boolean loop) {
        this.music = music;
        this.loop = loop;
        this.baseVolume = volume;
        music.setVolume(volume);
        music.setLooping(loop);
    }

    @Override public void play() { music.play(); }
    @Override public void stop() { music.stop(); }
    @Override public void dispose() { music.dispose(); }
    @Override public boolean isPlaying() { return music.isPlaying(); }

    @Override public void setInstanceName(String name) { this.instanceName = name; }
    @Override public String getInstanceName() { return instanceName; }

    @Override public void setPosition(Vector3 pos) { this.position.set(pos); }
    @Override public Vector3 getPosition() { return position; }

    @Override public void setAttachedObjectId(String id) { this.attachedToObjectId = id; }
    @Override public String getAttachedObjectId() { return attachedToObjectId; }

    @Override
    public void update3D(float volume, float pan) {
        music.setVolume(volume * baseVolume);
        music.setPan(pan, volume);
    }

    @Override
    public void setVolume(float volume) {
        music.setVolume(volume);
    }

    @Override
    public void setPitch(float pitch) {
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
