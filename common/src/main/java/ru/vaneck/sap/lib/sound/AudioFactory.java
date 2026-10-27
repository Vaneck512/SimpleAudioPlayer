package ru.vaneck.sap.lib.sound;

import ru.vaneck.sap.lib.AudioCacheManager;
import ru.vaneck.sap.lib.sound.model.AudioData;
import ru.vaneck.sap.lib.sound.model.AudioInstance;
import ru.vaneck.sap.lib.sound.model.SoundBuffer;
import ru.vaneck.sap.lib.sound.model.SoundInstance;
import ru.vaneck.sap.lib.sound.model.streaming.AudioStream;
import ru.vaneck.sap.lib.sound.model.streaming.StreamingSoundInstance;

import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static ru.vaneck.sap.lib.sound.AudioUtils.*;

public class AudioFactory {
    public static Executor EXECUTOR = Executors.newCachedThreadPool();
    public static CompletableFuture<AudioInstance> createSound(String source, AudioType type) {
        CompletableFuture<AudioInstance> future = new CompletableFuture<>();
        String cacheKey = source + type;
        AtomicReference<SoundBuffer> buffer = new AtomicReference<>();
        try {
            new Thread(() -> {
                try {
                    BufferedInputStream is = new BufferedInputStream(openStream(source, type));
                    AudioData audioData = AudioUtils.decodeStream(is, AudioUtils.parseStreamExtension(is));
                    future.completeAsync(() -> {
                        buffer.set(AudioCacheManager.getOrCreateBuffer(cacheKey, () -> createBuffer(audioData, true)));
                        SoundInstance instance = (SoundInstance) AudioCacheManager.track(new SoundInstance(false, false));
                        instance.setBuffer(buffer.get().getBufferId());
                        return instance;
                    }, EXECUTOR);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } catch (Exception e) {
            if (buffer.get() != null)
                buffer.get().cleanup();
            future.completeExceptionally(e);
            throw new SAPAudioException("Failed to create %s sound instance for source \"%s\", %s}".formatted(type, source, e.getMessage()));
        }
        return future;
    }

    public static CompletableFuture<AudioInstance> createStreamingSound(String source, AudioType type) {
        CompletableFuture<AudioInstance> future = new CompletableFuture<>();

        try {
            BufferedInputStream is = new BufferedInputStream(openStream(source, type));

            new Thread(() -> {
                AudioStream audioStream = null;
                try {
                    audioStream = AudioUtils.createStream(is, AudioUtils.parseStreamExtension(is), true);
                    AudioStream finalAudioStream = audioStream;

                    future.completeAsync(() -> {
                        try {
                            StreamingSoundInstance instance = (StreamingSoundInstance) AudioCacheManager.track(new StreamingSoundInstance(false, false));
                            instance.setStream(finalAudioStream);

                            return instance;
                        } catch (Exception e) {
                            try { finalAudioStream.close(); } catch (Exception ignored) {}
                            throw new RuntimeException(e);
                        }
                    }, EXECUTOR);

                } catch (Exception e) {
                    if (audioStream != null) {
                        try { audioStream.close(); } catch (Exception ignored) {}
                    } else {
                        try { is.close(); } catch (Exception ignored) {}
                    }
                    future.completeExceptionally(e);
                }
            }).start();

        } catch (Exception e) {
            future.completeExceptionally(e);
            throw new SAPAudioException("Failed to create %s sound instance for source \"%s\", %s}".formatted(type, source, e.getMessage()));
        }

        return future;
    }

    /**
     *
     * @param source path, resourceLocation or URL
     * @param type type of source
     * @param streaming read in chunks (if false - load all audio in memory)
     * @param settings set volume, position, max distance, ...
     * @throws Exception
     */
    public static void playOnce(String source, AudioType type, boolean streaming, Consumer<AudioInstance> settings) throws Exception {
        AudioInstance audioInstance = streaming ? createStreamingSound(source, type).get() : createSound(source, type).get();
        AudioCacheManager.autoClose(audioInstance);
        settings.accept(audioInstance);
        audioInstance.play();
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
