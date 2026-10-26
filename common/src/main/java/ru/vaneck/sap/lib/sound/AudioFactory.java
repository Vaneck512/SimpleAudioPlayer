package ru.vaneck.sap.lib.sound;

import javazoom.jl.decoder.*;
import ru.vaneck.sap.CommonClass;
import ru.vaneck.sap.lib.sound.model.AudioData;
import ru.vaneck.sap.lib.sound.model.SoundBuffer;
import ru.vaneck.sap.lib.sound.model.SoundInstance;

import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static ru.vaneck.sap.lib.sound.AudioUtils.*;

public class AudioFactory {
    public static Executor EXECUTOR = Executors.newCachedThreadPool();
    public static CompletableFuture<SoundInstance> createSound(String source, AudioType type) {
        CompletableFuture<SoundInstance> future = new CompletableFuture<>();
        String cacheKey = source + type;
        AtomicReference<SoundBuffer> buffer = new AtomicReference<>();
        try {
            BufferedInputStream is = new BufferedInputStream(openStream(source, type));
            new Thread(() -> {
                AudioData audioData = null;
                try {
                    audioData = AudioUtils.decodeStream(is, AudioUtils.parseStreamExtension(is));
                    AudioData finalAudioData = audioData;
                    future.completeAsync(() -> {
                        buffer.set(CommonClass.CACHE_MANAGER.getOrCreateBuffer(cacheKey, () -> createBuffer(finalAudioData, true)));
                        SoundInstance instance = new SoundInstance(false, false);
                        instance.setBuffer(buffer.get().getBufferId());
                        CommonClass.CACHE_MANAGER.trackInstance(instance);
                        return instance;
                    }, EXECUTOR);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } catch (Exception e) {
            buffer.get().cleanup();
            future.completeExceptionally(e);
            throw new SAPAudioException("Failed to create %s sound instance for source \"%s\", %s}".formatted(type, source, e.getMessage()));
        }
        return future;
    }

    public static SoundBuffer createBuffer(AudioData data, boolean mono) {
        ByteBuffer byteBuffer = data.data();
        AudioFormat format = data.format();

        // to play 3d sound in world
        if (mono) {
            byteBuffer = AudioUtils.convertBufferToMono(byteBuffer, format);
            format = new AudioFormat(
                    format.getSampleRate(),
                    format.getSampleSizeInBits(),
                    1,
                    true,
                    format.isBigEndian()
            );
        }

        return new SoundBuffer(byteBuffer, format);
    }
}
