package ru.vaneck.sap.lib.sound.model;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.io.Closeable;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface AudioInstance extends Closeable {
    /**
     * Checks if the audio resource or stream has been closed or disposed of.
     *
     * @return {@code true} if closed, {@code false} otherwise
     */
    boolean isClosed();

    /**
     * Starts or resumes or restarts playback of the audio instance.
     *
     * @return the current {@link AudioInstance} instance, for method chaining
     */
    AudioInstance play();

    /**
     * Pauses the current audio playback.
     *
     * @return the current {@link AudioInstance} instance, for method chaining
     */
    AudioInstance pause();

    /**
     * Stops the audio playback entirely and resets its position (next start: 00:00).
     *
     * @return the current {@link AudioInstance} instance, for method chaining
     */
    AudioInstance stop();

    /**
     * Sets the playback volume level.
     *
     * @param volume the gain/volume multiplier value
     * @return the current {@link AudioInstance} instance, for method chaining
     */
    AudioInstance setVolume(float volume);

    void setSoundChannel(@NotNull SoundSource channel);

    @NotNull SoundSource getSoundChannel();

    /**
     * Sets the 3D coordinates of the sound source in space.
     *
     * @param pos the 3D vector representing the position
     * @return the current {@link AudioInstance} instance, for method chaining
     */
    AudioInstance setPosition(Vector3f pos);

    /**
     * Sets the 3D coordinates of the sound source in space.
     *
     * @param pos the 3D vector representing the position
     * @return the current {@link AudioInstance} instance, for method chaining
     */
    AudioInstance setPosition(Vec3 pos);

    /**
     * Sets the source velocity solely for the purpose of calculating the Doppler effect.
     * It does not automatically move the sound in space.
     *
     * @param vec the velocity vector
     * @return the current {@link AudioInstance} instance, for method chaining
     */
    AudioInstance setVelocity(Vector3f vec);

    /**
     * Enables or disables continuous looping for the audio.
     *
     * @param loop {@code true} to loop infinitely, {@code false} to play once
     * @return the current {@link AudioInstance} instance, for method chaining
     */
    AudioInstance setLooping(boolean loop);

    /**
     * Sets relative coordinates for the sound source.
     *
     * @param relative {@code false} — 0 0 0 coordinates will be at the 0 0 0 block in the world;
     *                 {@code true} — 0 0 0 coordinates will be at the player's "head"
     * @return the current {@link AudioInstance} instance, for method chaining
     */
    AudioInstance setRelative(boolean relative);

    /**
     * Sets the distance at which sound attenuation begins to apply.
     *
     * @param distance the distance threshold value
     * @return the current {@link AudioInstance} instance, for method chaining
     */
    AudioInstance setMaxDistance(float distance);

    /**
     * Sets the maximum distance for the compression attenuation model.
     *
     * @param distance the reference distance value
     * @return the current {@link AudioInstance} instance, for method chaining
     */
    AudioInstance setReferenceDistance(float distance);

    /**
     * Sets the distance-based sound attenuation model.
     * <br>default in SAP - {@code AL11#AL_LINEAR_DISTANCE_CLAMPED}
     *
     * @param distanceModel the OpenAL attenuation model identifier (e.g., {@code AL10.AL_INVERSE_DISTANCE_CLAMPED})
     * @return the current {@link AudioInstance} instance, for method chaining
     */
    AudioInstance setAttenuationModel(int distanceModel);

    /**
     * Checks if the audio is currently actively playing.
     *
     * @return {@code true} if playing, {@code false} otherwise
     */
    boolean isPlaying();

    /**
     * Checks if the audio playback is currently paused.
     *
     * @return {@code true} if paused, {@code false} otherwise
     */
    boolean isPaused();

    /**
     * Checks if the audio has been triggered or started at least once.
     *
     * @return {@code true} if started, {@code false} otherwise
     */
    boolean isStarted();

    /**
     * checks whether an audio instance can be played at the specified coordinates
     * @return true/false if positional or not
     */
    boolean isPositional();
}
