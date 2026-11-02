package ru.vaneck.sap.lib;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import ru.vaneck.sap.Constants;
import ru.vaneck.sap.lib.sound.AudioFactory;
import ru.vaneck.sap.lib.sound.model.AudioInstance;
import ru.vaneck.sap.lib.sound.model.SoundBuffer;
import ru.vaneck.sap.lib.sound.model.SoundInstance;
import ru.vaneck.sap.lib.sound.model.streaming.StreamingSoundInstance;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Audio buffer cache with automatic cleanup.
 * <p>
 * Multiple identical instances (sources) can be started, but a unique
 * buffer for each source will be created only once.
 */
public class AudioCacheManager {
    private static final ConcurrentHashMap<String, SoundBuffer> SOUND_BUFFERS = new ConcurrentHashMap<>();
    private static final Set<SoundInstance> SOUND_INSTANCES = ConcurrentHashMap.newKeySet();
    private static final Set<StreamingSoundInstance> STREAMING_SOUND_INSTANCES = ConcurrentHashMap.newKeySet();

    private static final Set<AudioInstance> ACTIVE_INSTANCES = ConcurrentHashMap.newKeySet();

    private static final ScheduledExecutorService THREAD = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "SAP audio cleanup");
        thread.setDaemon(true);
        return thread;
    });

    public static SoundBuffer getOrCreateBuffer(String key, Supplier<SoundBuffer> creation) {
        return SOUND_BUFFERS.compute(key, (s, soundBuffer) -> {
            if (soundBuffer == null || soundBuffer.isClosed()) {
                return  creation.get();
            }
            return soundBuffer;
        });
    }

    /**
     * Tracking and closing
     * OpenAL sources upon
     * completion of playback
     */
    public static AudioInstance autoClose(AudioInstance audioInstance) {
        if (audioInstance instanceof SoundInstance soundInstance)
            SOUND_INSTANCES.add(soundInstance);
        else if (audioInstance instanceof StreamingSoundInstance streamingSoundInstance)
            STREAMING_SOUND_INSTANCES.add(streamingSoundInstance);

        return audioInstance;
    }

    /**
     * Track all SoundSources
     * to hold their Buffers in cache
     * there's no auto closing
     */
    public static AudioInstance track(AudioInstance instance) {
        ACTIVE_INSTANCES.add(instance);
        return instance;
    }

    static {
        THREAD.scheduleAtFixedRate(() -> Minecraft.getInstance().execute(AudioCacheManager::cleanUp), 250, 250, TimeUnit.MILLISECONDS);
    }

    public static void cleanUp() {
        SOUND_INSTANCES.removeIf(soundInstance -> {
            if (!soundInstance.isStarted()) return false;
            if (!soundInstance.isPlaying() && !soundInstance.isPaused()) {
                if (!soundInstance.isClosed()) {
                    soundInstance.close();
                    return true;
                }
            }
            return false;
        });

        ACTIVE_INSTANCES.removeIf(soundInstance -> {
            if (!soundInstance.isStarted()) return false;
            if (!soundInstance.isPlaying() && !soundInstance.isPaused()) {
                return !soundInstance.isClosed();
            }
            return false;
        });

        Set<Integer> activeBuffers = new HashSet<>();
        for (SoundInstance soundInstance : SOUND_INSTANCES) {
            int bufferId = AL11.alGetSourcei(soundInstance.getSourceId(), AL10.AL_BUFFER);
            activeBuffers.add(bufferId);
        }
        for (AudioInstance instance : ACTIVE_INSTANCES) {
            if (instance instanceof SoundInstance soundInstance) {
                int bufferId = AL11.alGetSourcei(soundInstance.getSourceId(), AL10.AL_BUFFER);
                activeBuffers.add(bufferId);
            }
        }

        SOUND_BUFFERS.entrySet().removeIf(entries -> {
            SoundBuffer buffer = entries.getValue();
            if (!activeBuffers.contains(buffer.getBufferId())) {
                if (!buffer.isClosed()) buffer.close();
                return true;
            }
            return false;
        });

        STREAMING_SOUND_INSTANCES.removeIf(streamingSoundInstance -> {
            if (streamingSoundInstance.isClosed()) return true;

            if (streamingSoundInstance.isStarted() && streamingSoundInstance.isEnded()) {
                try {
                    streamingSoundInstance.close();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!streamingSoundInstance.isClosed()) {
                streamingSoundInstance.update();
            }

            return false;
        });
    }

    public static void stopAndClearAllPositionalInstances() {
        SOUND_INSTANCES.removeIf(AudioCacheManager::removeIfPositional);
        STREAMING_SOUND_INSTANCES.removeIf(AudioCacheManager::removeIfPositional);
        ACTIVE_INSTANCES.removeIf(AudioCacheManager::removeIfPositional);
    }

    public static void clearAll() {
        Constants.LOG.info("Starting audio cleanup");

        THREAD.shutdownNow();
        AudioFactory.shutdown();
        SAP.shutdown();

//        Constants.LOG.info("Closing active instances");
        ACTIVE_INSTANCES.removeIf(AudioCacheManager::removeIf);

//        Constants.LOG.info("Closing streaming instances");
        STREAMING_SOUND_INSTANCES.removeIf(AudioCacheManager::removeIf);

//        Constants.LOG.info("Closing sound instances");
        SOUND_INSTANCES.removeIf(AudioCacheManager::removeIf);

//        Constants.LOG.info("Closing sound buffers");
        SOUND_BUFFERS.entrySet().removeIf(entry -> {
            SoundBuffer buffer = entry.getValue();

            if (!buffer.isClosed()) {
                buffer.close();
            }

            return true;
        });

        Constants.LOG.info("Audio cleanup completed");
    }

    private static boolean removeIfPositional(AudioInstance instance) {
        if (instance.isPositional()) {
            try {
                instance.close();
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private static boolean removeIf(AudioInstance instance) {
        try {
            instance.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
