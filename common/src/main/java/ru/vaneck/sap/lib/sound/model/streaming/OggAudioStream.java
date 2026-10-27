package ru.vaneck.sap.lib.sound.model.streaming;

import javazoom.jl.decoder.Header;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import static org.lwjgl.stb.STBVorbis.*;

public final class OggAudioStream implements AudioStream {
    private final long handle;
    private final int channels;
    private final int sampleRate;
    private final ByteBuffer encodedData;
    private final boolean forceMono;
    private boolean closed;

    public OggAudioStream(InputStream in, boolean forceMono) throws IOException {
        this.encodedData = readAllBytesToDirectBuffer(in);

        int[] error = new int[1];
        this.handle = stb_vorbis_open_memory(encodedData, error, null);
        if (handle == 0L) {
            MemoryUtil.memFree(encodedData);
            throw new IOException("Cannot open OGG from stream (STB error " + error[0] + ")");
        }

        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            stb_vorbis_get_info(handle, info);
            this.channels = info.channels();
            this.sampleRate = info.sample_rate();
        } catch (RuntimeException e) {
            stb_vorbis_close(handle);
            MemoryUtil.memFree(encodedData);
            throw e;
        }

        this.forceMono = forceMono;
    }

    @Override public int channels() { return channels; }
    @Override public int sampleRate() { return sampleRate; }

    @Override
    public int read(ShortBuffer pcm) {
        if (closed) throw new IllegalStateException("Closed");

        if (forceMono && channels == 2) {

            ShortBuffer stereoScratch = org.lwjgl.BufferUtils.createShortBuffer(pcm.remaining() * 2);
            int readFrames = stb_vorbis_get_samples_short_interleaved(handle, channels, stereoScratch);
            if (readFrames <= 0) return -1;

            for (int i = 0; i < readFrames; i++) {
                int left = stereoScratch.get(i * 2);
                int right = stereoScratch.get(i * 2 + 1);
                pcm.put((short) ((left + right) / 2));
            }
            return readFrames;
        } else {

            int read = stb_vorbis_get_samples_short_interleaved(handle, channels(), pcm);
            return read == 0 ? -1 : read;
        }
    }

    @Override
    public boolean isMono() {
        return this.forceMono;
    }

    @Override
    public void rewind() {
        if (closed) throw new IllegalStateException("Closed");
        if (!stb_vorbis_seek_start(handle)) {
            throw new IllegalStateException("Could not rewind OGG");
        }
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            stb_vorbis_close(handle);
            MemoryUtil.memFree(encodedData);
        }
    }

    private static ByteBuffer readAllBytesToDirectBuffer(InputStream in) throws IOException {
        byte[] bytes = in.readAllBytes();
        ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return buffer;
    }
}