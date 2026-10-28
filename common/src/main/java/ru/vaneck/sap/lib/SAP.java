package ru.vaneck.sap.lib;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import ru.vaneck.sap.Constants;
import ru.vaneck.sap.lib.sound.AudioFactory;
import ru.vaneck.sap.lib.sound.AudioType;
import ru.vaneck.sap.lib.sound.model.AudioInstance;
import ru.vaneck.sap.lib.sound.model.OpenAlUtils;

import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class SAP {

    private static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool(runnable -> {
                Thread thread = new Thread(runnable, "SAP waiter executor");
                thread.setDaemon(true);
                return thread;
            });

    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }

    public static void playPositional(@NotNull String uri, @NotNull AudioType uri_type, @NotNull Consumer<AudioInstance> settings) {
        play(uri, uri_type, true, false, settings);
    }

    public static void playStreamPositional(@NotNull String uri, @NotNull AudioType uri_type, @NotNull Consumer<AudioInstance> settings) {
        play(uri, uri_type, true, true, settings);
    }

    public static void playPositional(@NotNull String uri, @NotNull AudioType uri_type, float volume) {
        play(uri, uri_type, true, false, i -> i.setVolume(volume));
    }

    public static void playStreamPositional(@NotNull String uri, @NotNull AudioType uri_type, float volume) {
        play(uri, uri_type, true, true, i -> i.setVolume(volume));
    }

    public static void playPositional(@NotNull String uri, @NotNull AudioType uri_type, Vector3f position, float volume) {
        play(uri, uri_type, true, false,
                i -> {
                    i.setVolume(volume);
                    i.setPosition(position);
                }
        );
    }

    public static void playStreamPositional(@NotNull String uri, @NotNull AudioType uri_type, Vector3f position, float volume) {
        play(uri, uri_type, true, true,
                i -> {
                    i.setVolume(volume);
                    i.setPosition(position);
                }
        );
    }

    public static void play(@NotNull String uri, @NotNull AudioType uri_type) {
        play(uri, uri_type, false, false, null);
    }

    public static void playStream(@NotNull String uri, @NotNull AudioType uri_type) {
        play(uri, uri_type, false, true, null);
    }

    public static void play(@NotNull String uri, @NotNull AudioType uri_type, @NotNull Consumer<AudioInstance> settings) {
        play(uri, uri_type, false, false, settings);
    }

    public static void playStream(@NotNull String uri, @NotNull AudioType uri_type, @NotNull Consumer<AudioInstance> settings) {
        play(uri, uri_type, false, true, settings);
    }

    public static void play(@NotNull String uri, @NotNull AudioType uri_type, float volume) {
        play(uri, uri_type, false, false, i -> i.setVolume(volume));
    }

    public static void playStream(@NotNull String uri, @NotNull AudioType uri_type, float volume) {
        play(uri, uri_type, false, true, i -> i.setVolume(volume));
    }

    public static void play(@NotNull String uri, @NotNull AudioType uri_type, float volume, @NotNull Consumer<AudioInstance> settings) {
        play(uri, uri_type, false, false, settings.andThen(i -> i.setVolume(volume)));
    }

    public static void playStream(@NotNull String uri, @NotNull AudioType uri_type, float volume, @NotNull Consumer<AudioInstance> settings) {
        play(uri, uri_type, false, true, settings.andThen(i -> i.setVolume(volume)));
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
    public static void play(@NotNull String uri, @NotNull AudioType uri_type, boolean positional, boolean stream, @Nullable Consumer<AudioInstance> settings) {
        CompletableFuture.runAsync(() -> {
            while (!OpenAlUtils.isOpenAlReady()) {
                try {
                    Thread.sleep(512);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Constants.LOG.error("Thread Interrupted", e);
                }
            }
//            try {
                AudioFactory.playOnce(uri, uri_type, positional, stream, settings);
//            } catch (SocketTimeoutException e) {
//                Constants.LOG.error("Read timed out, check your url or connection. Message: {}", e.getMessage());
//            } catch (Throwable e) {
//                Constants.LOG.error("Failed to play \"{}\". {}", uri, e.getMessage(), e);
//            }
        }, EXECUTOR);
    }

    public boolean isReadyForPlaying() {
        return OpenAlUtils.isOpenAlReady();
    }
}
