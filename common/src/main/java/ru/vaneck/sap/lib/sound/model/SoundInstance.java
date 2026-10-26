package ru.vaneck.sap.lib.sound.model;

import org.joml.Vector3f;
import org.lwjgl.openal.AL10;

import static org.lwjgl.openal.AL10.*;

public class SoundInstance implements AudioInstance {

    private final int sourceId;
    private boolean closed;
    private boolean started;

    public SoundInstance(boolean loop, boolean relative) {
        this.sourceId = alGenSources();
        alSourcei(sourceId, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
        alSourcei(sourceId, AL_SOURCE_RELATIVE, relative ? AL_TRUE : AL_FALSE);
    }

    public void cleanup() {
        stop();
        alDeleteSources(sourceId);
    }

    public boolean isPlaying() {
        return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
    }

    public void pause() {
        alSourcePause(sourceId);
    }

    public void play() {
        alSourcePlay(sourceId);
        started = true;
    }

    public void setBuffer(int bufferId) {
        stop();
        alSourcei(sourceId, AL_BUFFER, bufferId);
    }

    public void setGain(float gain) {
        alSourcef(sourceId, AL_GAIN, gain);
    }

    public void setPosition(Vector3f position) {
        alSource3f(sourceId, AL_POSITION, position.x, position.y, position.z);
    }

    public void stop() {
        alSourceStop(sourceId);
    }

    public int getSourceId() {
        return sourceId;
    }

    public boolean isPaused() {
        return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PAUSED;
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    public boolean isStarted() {
        return started;
    }

    @Override
    public void close() {
        AL10.alSourceStop(this.sourceId);
        AL10.alDeleteSources(this.sourceId);
        this.closed = true;
    }
}