package ru.vaneck.sap.lib.sound;

import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class MinecraftSettingsListeners {
    public static Map<Long, BiConsumer<SoundSource, Float>> VOLUME_LISTENERS = new ConcurrentHashMap<>();

    private static long idCountVolumeListeners = 0L;

    /**
     * Registers the listener and returns the ID of the listener, to be able to unregister it later.<br>
     * Volume listeners get notified after the volume of a {@link SoundSource} got changed. <br>
     * Copyright © 2023-2026 Keksuccino
     */
    public static long registerVolumeListener(@NotNull BiConsumer<SoundSource, Float> listener) {
        Objects.requireNonNull(listener);
        idCountVolumeListeners++;
        VOLUME_LISTENERS.put(idCountVolumeListeners, listener);
        return idCountVolumeListeners;
    }

    public static void unregisterVolumeListener(long volumeListenerId) {
        VOLUME_LISTENERS.remove(volumeListenerId);
    }

    public static List<BiConsumer<SoundSource, Float>> getVolumeListeners() {
        return new ArrayList<>(VOLUME_LISTENERS.values());
    }
}
