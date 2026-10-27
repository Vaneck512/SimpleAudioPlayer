package ru.vaneck.sap.lib.sound.model;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface AudioInstance {
    boolean isClosed();
    AudioInstance play();
    AudioInstance pause();
    AudioInstance stop();
    AudioInstance setVolume(float volume);
    AudioInstance setPosition(Vector3f pos);
    AudioInstance setPosition(Vec3 pos);
    AudioInstance setVelocity(Vector3f vec);
    AudioInstance setLooping(boolean loop);
    AudioInstance maxDistance(float distance);
    AudioInstance ReferenceDistance(float distance);
    boolean isPlaying();
    boolean isPaused();
}
