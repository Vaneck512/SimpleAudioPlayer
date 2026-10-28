package ru.vaneck.sap.lib.sound;

import org.jspecify.annotations.Nullable;
import ru.vaneck.sap.Constants;
import ru.vaneck.sap.lib.AudioCacheManager;
import ru.vaneck.sap.lib.sound.model.*;
import ru.vaneck.sap.lib.sound.model.streaming.AudioStream;
import ru.vaneck.sap.lib.sound.model.streaming.StreamingSoundInstance;

import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static ru.vaneck.sap.lib.sound.AudioUtils.*;

public class AudioFactory {
    public static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool(runnable -> {
                Thread thread = new Thread(runnable, "SAP audio worker");
                thread.setDaemon(true);
                return thread;
            });
    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }

    public static CompletableFuture<AudioInstance> createSound(String source, AudioType type, boolean positional) {
        CompletableFuture<AudioInstance> future = new CompletableFuture<>();
        if (notReady()) {
            future.completeExceptionally(new ALException("Engine not ready"));
            return future;
        }
        String cacheKey = source + ":" + type + "&p=" + positional;
        AtomicReference<SoundBuffer> buffer = new AtomicReference<>();
        try {
            EXECUTOR.execute(() -> {
                try {
                    BufferedInputStream is = new BufferedInputStream(openStream(source, type));
                    AudioData audioData = AudioUtils.decodeStream(is, AudioUtils.parseStreamExtension(is));
                    future.completeAsync(() -> {
                        buffer.set(AudioCacheManager.getOrCreateBuffer(cacheKey, () -> createBuffer(audioData, positional)));
                        SoundInstance instance = (SoundInstance) AudioCacheManager.track(new SoundInstance(false, false));
                        instance.setBuffer(buffer.get().getBufferId());
                        return instance;
                    }, EXECUTOR);
                } catch (Exception e) {
                    throw new SAPAudioException(e.getMessage());
                }
            });
        } catch (Exception e) {
            if (buffer.get() != null)
                buffer.get().cleanup();
            future.completeExceptionally(e);
            throw new SAPAudioException("Failed to create %s sound instance for source \"%s\", %s}".formatted(type, source, e.getMessage()));
        }
        return future;
    }

    public static CompletableFuture<AudioInstance> createStreamingSound(String source, AudioType type, boolean positional) {
        CompletableFuture<AudioInstance> future = new CompletableFuture<>();

        if (notReady()) {
            future.completeExceptionally(new ALException("Engine not ready"));
            return future;
        }

        try {
            BufferedInputStream is = new BufferedInputStream(openStream(source, type));

            EXECUTOR.execute(() -> {
                AudioStream audioStream = null;
                try {
                    audioStream = AudioUtils.createStream(is, AudioUtils.parseStreamExtension(is), positional);
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
            });

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
     * @param positoinal play in world (.setPosition() on instance) insted of playing in head
     * @param streaming read in chunks (if false - load all audio in memory)
     * @param settings set volume, position, max distance, ...
     */
    public static void playOnce(String source, AudioType type, boolean positoinal, boolean streaming, @Nullable Consumer<AudioInstance> settings) throws Throwable {
        CompletableFuture<AudioInstance> future = streaming ? createStreamingSound(source, type, positoinal) : createSound(source, type, positoinal);
        if (future.isCompletedExceptionally())
            throw future.exceptionNow();
        future.thenAccept(audioInstance -> {
            AudioCacheManager.autoClose(audioInstance);
            if (settings != null) settings.accept(audioInstance);
            audioInstance.play();
        });
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


    private static boolean notReady() {
        boolean ready = OpenAlUtils.isOpenAlReady();
        if (!ready) {
            Constants.LOG.warn("OpenAL is not ready, operation is stopped");
        }
        return !ready;
    }
}
