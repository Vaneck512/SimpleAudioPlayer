package ru.vaneck.sap.lib.sound.model.streaming;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public final class WavAudioStream implements AudioStream {
    private final byte[] rawBufferBytes;
    private AudioInputStream stream;
    private final int originalChannels;
    private final int sampleRate;
    private final boolean forceMono;

    private final byte[] readBuffer = new byte[8192];
    private boolean closed;

    public WavAudioStream(InputStream in, boolean forceMono) throws IOException, UnsupportedAudioFileException {
        this.forceMono = forceMono;

        this.rawBufferBytes = in.readAllBytes();
        in.close();

        this.stream = createAudioInputStream();
        AudioFormat format = stream.getFormat();

        this.originalChannels = format.getChannels();
        this.sampleRate = (int) format.getSampleRate();
    }

    private AudioInputStream createAudioInputStream() throws IOException, UnsupportedAudioFileException {
        ByteArrayInputStream bais = new ByteArrayInputStream(this.rawBufferBytes);
        AudioInputStream originalStream = AudioSystem.getAudioInputStream(bais);
        AudioFormat format = originalStream.getFormat();

        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED || format.getSampleSizeInBits() != 16) {
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), 16,
                    format.getChannels(), format.getChannels() * 2, format.getSampleRate(), false
            );
            return AudioSystem.getAudioInputStream(targetFormat, originalStream);
        }
        return originalStream;
    }

    @Override public int channels() { return forceMono ? 1 : originalChannels; }
    @Override public int sampleRate() { return sampleRate; }
    @Override public boolean isMono() { return channels() == 1; }

    @Override
    public int read(ShortBuffer pcm) {
        if (closed) throw new IllegalStateException("Closed");

        int bytesNeeded;
        if (forceMono && originalChannels == 2) {
            bytesNeeded = Math.min(readBuffer.length, pcm.remaining() * 4);
        } else {
            bytesNeeded = Math.min(readBuffer.length, pcm.remaining() * originalChannels * 2);
        }

        try {
            int bytesRead = stream.read(readBuffer, 0, bytesNeeded);
            if (bytesRead <= 0) return -1;

            ByteBuffer byteBuf = ByteBuffer.wrap(readBuffer, 0, bytesRead).order(ByteOrder.LITTLE_ENDIAN);

            if (forceMono && originalChannels == 2) {
                int framesRead = bytesRead / 4;
                for (int i = 0; i < framesRead; i++) {
                    short left = byteBuf.getShort();
                    short right = byteBuf.getShort();
                    short monoSample = (short) ((left + right) / 2);
                    pcm.put(monoSample);
                }
                return framesRead;
            } else {
                int shortsRead = bytesRead / 2;
                for (int i = 0; i < shortsRead; i++) {
                    pcm.put(byteBuf.getShort());
                }
                return shortsRead / originalChannels;
            }
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public void rewind() {
        if (closed) throw new IllegalStateException("Closed");
        try {

            try { this.stream.close(); } catch (IOException ignored) {}

            this.stream = createAudioInputStream();
        } catch (Exception e) {
            throw new IllegalStateException("Could not rewind WAV stream securely", e);
        }
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            try { stream.close(); } catch (IOException ignored) {}
        }
    }
}