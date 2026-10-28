package ru.vaneck.sap.lib.sound.model.streaming;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import ru.vaneck.sap.lib.sound.model.AudioInstance;

import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.system.MemoryUtil.*;

public final class StreamingSoundInstance implements AutoCloseable, AudioInstance {
    private static final int BUFFER_COUNT = 4;
    private static final int CHUNK_FRAMES = 8192;

    private final int sourceId;
    private final int[] buffers = new int[BUFFER_COUNT];
    private final ShortBuffer pcm;

    private AudioStream stream;
    private boolean loop;
    private boolean active;
    private boolean closed;
    private boolean started;
    private boolean ended;

    public StreamingSoundInstance(boolean loop, boolean relative) {
        this.loop = loop;
        sourceId = alGenSources();
        if (sourceId == 0) throw new IllegalStateException("Could not create OpenAL source");

        int made = 0;
        ShortBuffer scratch = null;

        try {
            for (int i = 0; i < BUFFER_COUNT; i++) {
                int id = alGenBuffers();
                if (id == 0) throw new IllegalStateException("Could not create OpenAL buffer");
                buffers[i] = id;
                made++;
            }

            java.nio.ByteBuffer byteBuf = org.lwjgl.system.MemoryUtil.memAlloc(CHUNK_FRAMES * 2 * 2);
            scratch = byteBuf.order(java.nio.ByteOrder.nativeOrder()).asShortBuffer();

            alSourcei(sourceId, AL_SOURCE_RELATIVE, relative ? AL_TRUE : AL_FALSE);
            alSourcei(sourceId, AL_LOOPING, AL_FALSE);
        } catch (RuntimeException e) {
            if (scratch != null) memFree(scratch);
            for (int i = 0; i < made; i++) alDeleteBuffers(buffers[i]);
            alDeleteSources(sourceId);
            throw e;
        }
        pcm = scratch;

//        if (!relative) {
            alSourcei(
                    this.sourceId,
                    AL_DISTANCE_MODEL,
                    AL11.AL_LINEAR_DISTANCE_CLAMPED
            );
//        }
    }

    public void setStream(AudioStream stream) {
        this.stream = stream;
    }

    private boolean fill(int bufferId) {
        if (stream == null) return false;

        int channels = stream.channels();
        boolean retriedAtStart = false;

        pcm.clear();
        pcm.limit(CHUNK_FRAMES * channels);

        int totalShortsWritten = 0;

        while (pcm.hasRemaining()) {
            int read = stream.read(pcm);

            if (read > 0) {
                totalShortsWritten += read;
                retriedAtStart = false;
                continue;
            }

            if (read < 0) {
                if (!loop || retriedAtStart) break;

                stream.rewind();
                retriedAtStart = true;
            } else {

                break;
            }
        }

        if (totalShortsWritten == 0) return false;

        pcm.flip();

        int format = stream.isMono() ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;

        alBufferData(bufferId, format, pcm, stream.sampleRate());
        return true;
    }

    public StreamingSoundInstance play() {
        ensureOpen();
        if (stream == null) throw new IllegalStateException("Call setStream() first");

        if (alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PAUSED) {
            active = true;
            alSourcePlay(sourceId);
            return this;
        }

        if (alGetSourcei(sourceId, AL_BUFFERS_QUEUED) == 0) {
            stream.rewind();
            this.ended = false;
            for (int buffer : buffers) {
                if (!fill(buffer)) {
                    this.ended = true;
                    break;
                }
                alSourceQueueBuffers(sourceId, buffer);
            }
        }
        active = alGetSourcei(sourceId, AL_BUFFERS_QUEUED) > 0;
        if (active) alSourcePlay(sourceId);

        this.started = true;
        return this;
    }

    public void update() {
        if (!active || closed) return;
        int processed = alGetSourcei(sourceId, AL_BUFFERS_PROCESSED);
        while (processed-- > 0) {
            int buffer = alSourceUnqueueBuffers(sourceId);

            if (fill(buffer)) {
                alSourceQueueBuffers(sourceId, buffer);
            } else {
                this.ended = true;
            }
        }

        int queued = alGetSourcei(sourceId, AL_BUFFERS_QUEUED);
        if (queued == 0 && this.ended) {
            active = false;
        } else if (alGetSourcei(sourceId, AL_SOURCE_STATE) != AL_PLAYING && !this.ended) {

            alSourcePlay(sourceId);
        }
    }

    public boolean isEnded() {
        return this.ended && alGetSourcei(sourceId, AL_BUFFERS_QUEUED) == 0;
    }

    public boolean isStarted() {
        return started;
    }

    @Override
    public StreamingSoundInstance pause() {
        ensureOpen();
        active = false;
        alSourcePause(sourceId);
        return this;
    }

    @Override
    public StreamingSoundInstance stop() {
        ensureOpen();
        active = false;
        alSourceStop(sourceId);
        int queued = alGetSourcei(sourceId, AL_BUFFERS_QUEUED);
        while (queued-- > 0) alSourceUnqueueBuffers(sourceId);
        if (stream != null) stream.rewind();
        return this;
    }

    public boolean isPlaying() {
        return !closed && alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
    }
    public boolean isPaused() {
        return !closed && alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PAUSED;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public StreamingSoundInstance setLooping(boolean loop) {
        alSourcei(sourceId, AL_LOOPING, AL_FALSE);
        this.loop = loop;
        return this;
    }

    @Override
    public StreamingSoundInstance setRelative(boolean relative) {
        alSourcei(sourceId, AL_SOURCE_RELATIVE, relative ? AL_TRUE : AL_FALSE);
        return this;
    }

    @Override
    public StreamingSoundInstance maxDistance(float distance) {
        alSourcef(this.sourceId, AL_MAX_DISTANCE, distance);
        return this;
    }

    @Override
    public StreamingSoundInstance ReferenceDistance(float distance) {
        alSourcef(this.sourceId, AL_REFERENCE_DISTANCE, distance);
        return this;
    }

    @Override
    public StreamingSoundInstance setAttenuationModel(int distanceModel) {
        AL10.alSourcei(
                this.sourceId,
                AL_DISTANCE_MODEL,
                distanceModel
        );
        return this;
    }

    @Override
    public StreamingSoundInstance setPosition(Vector3f p) {
        ensureOpen();
        alSource3f(sourceId, AL_POSITION, p.x, p.y, p.z);
        return this;
    }

    @Override
    public AudioInstance setPosition(Vec3 p) {
        ensureOpen();
        alSource3f(sourceId, AL_POSITION, (float) p.x, (float) p.y, (float) p.z);
        return this;
    }

    @Override
    public StreamingSoundInstance setVelocity(Vector3f v) {
        ensureOpen();
        alSource3f(sourceId, AL_VELOCITY, v.x, v.y, v.z);
        return this;
    }

    @Override
    public StreamingSoundInstance setVolume(float gain) {
        ensureOpen();
        alSourcef(sourceId, AL_GAIN, gain);
        return this;
    }

    public void setProperty(int param, float value) {
        ensureOpen();
        alSourcef(sourceId, param, value);
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("SoundSource is closed");
    }

//    public void cleanup() throws Exception {
//        close();
//    }

    @Override
    public void close() {
        if (closed) return;
        stop();
        closed = true;
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            stream = null;
        }
        for (int buffer : buffers) alDeleteBuffers(buffer);
        memFree(pcm);
        alDeleteSources(sourceId);
    }

    public boolean isClosed() {
        return this.closed;
    }
}