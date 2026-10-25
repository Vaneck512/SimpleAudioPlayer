package ru.vaneck.sap.lib.sound;

import org.joml.Vector3f;
import ru.vaneck.sap.lib.sound.decoders.OggDecoder;
import ru.vaneck.sap.lib.sound.model.AudioData;
import ru.vaneck.sap.lib.sound.model.SoundBuffer;
import ru.vaneck.sap.lib.sound.model.SoundInstance;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class AudioFactory {
    public static Executor EXECUTOR = Executors.newCachedThreadPool();
    public static CompletableFuture<SoundInstance> createSound(String source, AudioType type) {
        CompletableFuture<SoundInstance> future = new CompletableFuture<>();
        AtomicReference<SoundBuffer> buffer = new AtomicReference<>();
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(source));
            AudioData audioData = new OggDecoder().decode(is);
            future.completeAsync(() -> {
                buffer.set(new SoundBuffer(audioData.data(), audioData.format()));
                SoundInstance instance = new SoundInstance(false, false);
                instance.setBuffer(buffer.get().getBufferId());
                return instance;
            }, EXECUTOR);
            is.close();
        } catch (Exception e) {
            buffer.get().cleanup();
            future.completeExceptionally(e);
            throw new SAPAudioException("Failed to create %s sound instance for source \"%s\", %s}".formatted(type, source, e.getMessage()));
        }
        return future;
    }
}
