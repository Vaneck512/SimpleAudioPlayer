package ru.vaneck.sap.lib.sound.model;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import ru.vaneck.sap.lib.sound.MinecraftSettingsListeners;

import java.io.Closeable;
import java.util.Objects;

import static org.lwjgl.openal.AL10.*;

// SoundSource
public class SoundInstance implements AudioInstance, Closeable {

    private final int sourceId;
    private boolean closed;
    private boolean started;
    protected SoundSource soundChannel = SoundSource.MASTER;
    private float volume = 1.0F;
    protected long volumeListenerId;

    public SoundInstance(boolean loop, boolean relative) {
        this.sourceId = alGenSources();
        alSourcei(sourceId, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
        alSourcei(sourceId, AL_SOURCE_RELATIVE, relative ? AL_TRUE : AL_FALSE);


        AL10.alSourcei(
                this.sourceId,
                AL_DISTANCE_MODEL,
                AL11.AL_LINEAR_DISTANCE_CLAMPED
        );

        this.volumeListenerId = MinecraftSettingsListeners.registerVolumeListener((soundSource, newVolume) -> {
            // Every channel's final volume includes the master volume.
            if (soundSource == this.soundChannel || soundSource == SoundSource.MASTER) this.setVolume(this.volume);
        });
        this.setVolume(this.volume);
    }

    public void cleanup() {
        stop();
        alDeleteSources(sourceId);
    }

    public boolean isPlaying() {
        return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
    }

    public SoundInstance pause() {
        alSourcePause(sourceId);
        return this;
    }

    public SoundInstance play() {
        alSourcePlay(sourceId);
        started = true;
        return this;
    }

    public void setBuffer(int bufferId) {
        stop();
        alSourcei(sourceId, AL_BUFFER, bufferId);
    }

    public SoundInstance setVolume(float volume) {
        this.volume = volume;
        float actualVolume = this.volume;
        float soundSourceVolume = Minecraft.getInstance().options.getFinalSoundSourceVolume(this.soundChannel);
        actualVolume = actualVolume * soundSourceVolume; //Calculate percentage of volume by this audio's sound channel
        alSourcef(sourceId, AL_GAIN, actualVolume);
        return this;
    }

    @Override
    public void setSoundChannel(@NotNull SoundSource channel) {
        this.soundChannel = Objects.requireNonNull(channel);
        this.setVolume(this.volume);
    }

    @Override
    public @NotNull SoundSource getSoundChannel() {
        return this.soundChannel;
    }

    @Override
    public SoundInstance setPosition(Vector3f position) {
        alSource3f(sourceId, AL_POSITION, position.x, position.y, position.z);
        return this;
    }

    @Override
    public SoundInstance setPosition(Vec3 position) {
        alSource3f(sourceId, AL_POSITION, (float) position.x, (float) position.y, (float) position.z);
        return this;
    }

    @Override
    public AudioInstance setVelocity(Vector3f vec) {
        alSource3f(sourceId, AL_VELOCITY, vec.x, vec.y, vec.z);
        return this;
    }

    @Override
    public SoundInstance setLooping(boolean loop) {
        alSourcei(sourceId, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
        return this;
    }

    @Override
    public SoundInstance setRelative(boolean relative) {
        alSourcei(sourceId, AL_SOURCE_RELATIVE, relative ? AL_TRUE : AL_FALSE);
        return this;
    }

    @Override
    public SoundInstance maxDistance(float distance) {
        alSourcef(this.sourceId, AL_MAX_DISTANCE, distance);
        return this;
    }

    @Override
    public SoundInstance setReferenceDistance(float distance) {
        alSourcef(this.sourceId, AL_REFERENCE_DISTANCE, distance);
        return this;
    }

    @Override
    public SoundInstance setAttenuationModel(int distanceModel) {
        AL10.alSourcei(
                this.sourceId,
                AL_DISTANCE_MODEL,
                distanceModel
        );
        return this;
    }

    public SoundInstance stop() {
        alSourceStop(sourceId);
        return this;
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
        if (closed) return;
        MinecraftSettingsListeners.unregisterVolumeListener(this.volumeListenerId);
        cleanup();
        this.closed = true;
    }
}