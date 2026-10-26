package ru.vaneck.sap.lib;

import ru.vaneck.sap.lib.sound.model.SoundBuffer;
import ru.vaneck.sap.lib.sound.model.SoundInstance;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class AudioCacheManager {
    private static final Cleaner CLEANER = Cleaner.create();

    private final Map<String, WeakReference<SoundBuffer>> bufferCache = new WeakHashMap<>();

    private final Map<String, SoundInstance> activeInstances = new ConcurrentHashMap<>();

    public synchronized SoundBuffer getOrCreateBuffer(String uniqueKey, Supplier<SoundBuffer> factory) {
        WeakReference<SoundBuffer> ref = bufferCache.get(uniqueKey);
        SoundBuffer buffer = (ref != null) ? ref.get() : null;

        if (buffer == null || buffer.isClosed()) {
            buffer = factory.get();

            final SoundBuffer finalBuffer = buffer;
            CLEANER.register(buffer, finalBuffer::close);

            bufferCache.put(uniqueKey, new WeakReference<>(buffer));
        }

        return buffer;
    }

    public void trackInstance(SoundInstance instance) {
        String instanceId = UUID.randomUUID().toString();
        activeInstances.put(instanceId, instance);
    }

    public void update() {
        if (activeInstances.isEmpty()) return;

        activeInstances.entrySet().removeIf(entry -> {
            SoundInstance instance = entry.getValue();

            if (!instance.isPlaying()) {
                instance.cleanup();
                return true;
            }
            return false;
        });
//        System.out.println(activeInstances.values());
        System.out.println(bufferCache.size());
    }

    public void clearAll() {
        synchronized (this) {
            for (WeakReference<SoundBuffer> ref : bufferCache.values()) {
                SoundBuffer buffer = ref.get();
                if (buffer != null && !buffer.isClosed()) {
                    buffer.close();
                }
            }
            bufferCache.clear();
        }

        activeInstances.values().forEach(SoundInstance::cleanup);
        activeInstances.clear();
    }
}
