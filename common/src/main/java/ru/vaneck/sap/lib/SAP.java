package ru.vaneck.sap.lib;

import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import ru.vaneck.sap.Constants;
import ru.vaneck.sap.lib.sound.AudioFactory;
import ru.vaneck.sap.lib.sound.AudioType;
import ru.vaneck.sap.lib.sound.model.AudioInstance;
import ru.vaneck.sap.lib.sound.model.OpenAlUtils;
import ru.vaneck.sap.lib.sound.model.SoundInstance;
import ru.vaneck.sap.lib.sound.model.streaming.StreamingSoundInstance;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class SAP {

    private static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool(runnable -> {
                Thread thread = new Thread(runnable, "SAP waiter executor");
                thread.setDaemon(true);
                return thread;
            });

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    public static void shutdown() {
        EXECUTOR.shutdownNow();
        SCHEDULED_EXECUTOR.shutdown();
    }

    public static void playPositional(@NotNull String uri, @NotNull AudioType uri_type, @NotNull Consumer<AudioInstance> settings) {
        play(uri, uri_type, true, false, null, settings);
    }

    public static void playStreamPositional(@NotNull String uri, @NotNull AudioType uri_type, @NotNull Consumer<AudioInstance> settings) {
        play(uri, uri_type, true, true, null, settings);
    }

    public static void playPositional(@NotNull String uri, @NotNull AudioType uri_type, float volume, float x, float y, float z) {
        play(uri, uri_type, true, false, null, i -> {
            i.setVolume(volume);
            i.setPosition(new Vector3f(x, y, z));
        });
    }

    public static void playStreamPositional(@NotNull String uri, @NotNull AudioType uri_type, float volume, float x, float y, float z) {
        play(uri, uri_type, true, true, null, i -> {
            i.setVolume(volume);
            i.setPosition(new Vector3f(x, y, z));
        });
    }

    public static void playPositional(@NotNull String uri, @NotNull AudioType uri_type, Vector3f position, float volume) {
        play(uri, uri_type, true, false, null,
                i -> {
                    i.setVolume(volume);
                    i.setPosition(position);
                }
        );
    }

    public static void playStreamPositional(@NotNull String uri, @NotNull AudioType uri_type, Vector3f position, float volume) {
        play(uri, uri_type, true, true, null,
                i -> {
                    i.setVolume(volume);
                    i.setPosition(position);
                }
        );
    }


    public static void play(@NotNull String uri, @NotNull AudioType uri_type, float volume, @NotNull Consumer<AudioInstance> settings) {
        play(uri, uri_type, false, false, null, settings.andThen(i -> i.setVolume(volume)));
    }

    public static void playStream(@NotNull String uri, @NotNull AudioType uri_type, float volume, @NotNull Consumer<AudioInstance> settings) {
        play(uri, uri_type, false, true, null, settings.andThen(i -> i.setVolume(volume)));
    }


    public static void play(@NotNull String uri, @NotNull AudioType uri_type, @NotNull Consumer<AudioInstance> settings) {
        play(uri, uri_type, false, false, null, settings);
    }

    public static void playStream(@NotNull String uri, @NotNull AudioType uri_type, @NotNull Consumer<AudioInstance> settings) {
        play(uri, uri_type, false, true, null, settings);
    }


    public static void play(@NotNull String uri, @NotNull AudioType uri_type, @NotNull SoundSource channel, float volume) {
        play(uri, uri_type, false, false, channel, i -> i.setVolume(volume));
    }

    public static void playStream(@NotNull String uri, @NotNull AudioType uri_type, @NotNull SoundSource channel, float volume) {
        play(uri, uri_type, false, true, channel, i -> i.setVolume(volume));
    }


    public static void play(@NotNull String uri, @NotNull AudioType uri_type, @NotNull SoundSource channel) {
        play(uri, uri_type, false, false, channel, null);
    }

    public static void playStream(@NotNull String uri, @NotNull AudioType uri_type, @NotNull SoundSource channel) {
        play(uri, uri_type, false, true, channel, null);
    }


    public static void play(@NotNull String uri, @NotNull AudioType uri_type, float volume) {
        play(uri, uri_type, false, false, null, i -> i.setVolume(volume));
    }

    public static void playStream(@NotNull String uri, @NotNull AudioType uri_type, float volume) {
        play(uri, uri_type, false, true, null, i -> i.setVolume(volume));
    }

    
    public static void play(@NotNull String uri, @NotNull AudioType uri_type) {
        play(uri, uri_type, false, false, null, null);
    }

    public static void playStream(@NotNull String uri, @NotNull AudioType uri_type) {
        play(uri, uri_type, false, true, null, null);
    }


    /**
     * Plays audio from the specified URI with the given parameters.
     *
     * @param uri the URI string of the audio file; URL, File path or ResourceLocation (Identifier)
     * @param uri_type the type of the URI;
     * @param positional indicates whether the sound is positional (3D sound, works in world)
     * @param stream determines whether the audio should be streamed rather than fully preloaded <br>
     *               !Do not create many stream instances at the same time or use this if you know what you're doing!
     * @param settings custom settings for the audio instance; F.eg. coordinates, loop, volume, ... . can be null
     */
    public static void play(@NotNull String uri, @NotNull AudioType uri_type, boolean positional, boolean stream, @Nullable SoundSource channel, @Nullable Consumer<AudioInstance> settings) {
        runOnSoundEngineLoaded(() -> {
            Consumer<AudioInstance> finalSettings = settings;
            if (channel != null && settings != null)
                finalSettings = settings.andThen(s -> s.setSoundChannel(channel));
            AudioFactory.playOnce(uri, uri_type, positional, stream, finalSettings);
        });
    }

    /**
     * Creates {@link SoundInstance} asynchronously wrapped in completable future <br>
     * You can pause, resume, or stop it, as well as set various parameters. <br>
     * Note: you must close unused instances manually <br>
     * @param uri the URI string of the audio file; URL, File path or ResourceLocation (Identifier)
     * @param type the type of the URI;
     * @param positional indicates whether the sound is positional (3D sound, works in world)
     * @return {@link   CompletableFuture}
     */
    public static CompletableFuture<SoundInstance> create(String uri, AudioType type, boolean positional) {
        return AudioFactory.createSound(uri, type, positional).thenApply(SoundInstance.class::cast).exceptionallyAsync(throwable -> {
            Constants.LOG.error("Exception while creating sound instance: {}", throwable.toString());
            return null;
        });
    }

    /**
     * Creates {@link StreamingSoundInstance} asynchronously wrapped in completable future <br>
     * You can pause, resume, or stop it, as well as set various parameters. <br>
     * Note: you must close unused instances manually <br>
     * @param uri the URI string of the audio file; URL, File path or ResourceLocation (Identifier)
     * @param type the type of the URI;
     * @param positional indicates whether the sound is positional (3D sound, works in world)
     * @return {@link   CompletableFuture}
     */
    public static CompletableFuture<StreamingSoundInstance> createStreaming(String uri, AudioType type, boolean positional) {
        return AudioFactory.createSound(uri, type, positional).thenApply(StreamingSoundInstance.class::cast).exceptionallyAsync(throwable -> {
            Constants.LOG.error("Exception while creating sound instance: {}", throwable.toString());
            return null;
        });
    }

    /**
     * Runnable will be executed automatically once Minecraft's sound engine has loaded
     * @param runnable function
     */
    public static void runOnSoundEngineLoaded(Runnable runnable) {
        AtomicReference<ScheduledFuture<?>> futureRef = new AtomicReference<>();

        ScheduledFuture<?> future = SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            try {
                if (OpenAlUtils.isOpenAlReady() && null != futureRef.get()) {
                    futureRef.get().cancel(true); // to stop next run
                    runnable.run();
                }
            } catch (Throwable exception) {
                futureRef.get().cancel(false);
                Constants.LOG.error("Exception while playing sound: {}", exception.toString());
            }
        }, 0, 512, TimeUnit.MILLISECONDS);

        futureRef.set(future);
    }

    public boolean isReadyForPlaying() {
        return OpenAlUtils.isOpenAlReady();
    }
}
