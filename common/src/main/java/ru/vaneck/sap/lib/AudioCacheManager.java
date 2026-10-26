package ru.vaneck.sap.lib;

import net.minecraft.client.Minecraft;
import org.apache.commons.compress.compressors.pack200.Pack200Utils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import ru.vaneck.sap.lib.sound.model.SoundBuffer;
import ru.vaneck.sap.lib.sound.model.SoundInstance;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
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

    public static SoundInstance autoClean(SoundInstance soundInstance) {
        System.out.println(SOUND_INSTANCES);
        System.out.println(SOUND_BUFFERS);
        SOUND_INSTANCES.add(soundInstance);
        return soundInstance;
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

        Set<Integer> activeBuffers = new HashSet<>();
        for (SoundInstance soundInstance : SOUND_INSTANCES) {
            int bufferId = AL11.alGetSourcei(soundInstance.getSourceId(), AL10.AL_BUFFER);
            activeBuffers.add(bufferId);
        }

        SOUND_BUFFERS.entrySet().removeIf(entries -> {
            SoundBuffer buffer = entries.getValue();
            if (!activeBuffers.contains(buffer.getBufferId())) {
                buffer.close();
                return true;
            }
            return false;
        });
    }

}
