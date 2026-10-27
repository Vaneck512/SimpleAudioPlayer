package ru.vaneck.sap.lib.sound.model.streaming;

import javazoom.jl.decoder.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ShortBuffer;

public final class Mp3AudioStream implements AudioStream {
    private final InputStream rawStream;
    private Bitstream bitstream;
    private final Decoder decoder;

    private final int originalChannels;
    private int currentSampleRate;
    private final boolean forceMono;

    private short[] currentFrameSamples;
    private int currentFrameLength = 0;
    private int currentFramePosition = 0;

    public Mp3AudioStream(InputStream in, boolean forceMono) throws IOException, BitstreamException {
        this.rawStream = in.markSupported() ? in : new BufferedInputStream(in);
        this.rawStream.mark(Integer.MAX_VALUE);

        this.bitstream = new Bitstream(rawStream);
        this.decoder = new Decoder();
        this.forceMono = forceMono;

        Header firstHeader = bitstream.readFrame();
        if (firstHeader == null) throw new IOException("Empty MP3 stream");

        this.currentSampleRate = firstHeader.frequency();
        this.originalChannels = (firstHeader.mode() == Header.SINGLE_CHANNEL) ? 1 : 2;

        bitstream.unreadFrame();
    }

    @Override public int channels() { return forceMono ? 1 : originalChannels; }
    @Override public int sampleRate() { return currentSampleRate; }
    @Override public boolean isMono() { return channels() == 1; }

    @Override
    public int read(ShortBuffer pcm) {
        int shortsWritten = 0;

        while (pcm.hasRemaining()) {
            if (currentFramePosition >= currentFrameLength) {
                if (!loadNextFrame()) {
                    return shortsWritten > 0 ? shortsWritten : -1;
                }
            }

            int jlayerChannels = originalChannels;

            if (forceMono) {
                if (jlayerChannels == 2) {

                    while (pcm.hasRemaining() && (currentFramePosition + 1) < currentFrameLength) {
                        int left = currentFrameSamples[currentFramePosition++];
                        int right = currentFrameSamples[currentFramePosition++];
                        short monoSample = (short) ((left + right) / 2);
                        pcm.put(monoSample);
                        shortsWritten++;
                    }
                } else {

                    while (pcm.hasRemaining() && currentFramePosition < currentFrameLength) {
                        pcm.put(currentFrameSamples[currentFramePosition++]);
                        shortsWritten++;
                    }
                }
            } else {

                if (jlayerChannels == 2) {

                    while (pcm.hasRemaining() && currentFramePosition < currentFrameLength) {
                        pcm.put(currentFrameSamples[currentFramePosition++]);
                        shortsWritten++;
                    }
                } else {

                    while (pcm.hasRemaining() && currentFramePosition < currentFrameLength) {
                        short sample = currentFrameSamples[currentFramePosition++];
                        pcm.put(sample);
                        pcm.put(sample);
                        shortsWritten += 2;
                    }
                }
            }
        }

        return shortsWritten;
    }

    private boolean loadNextFrame() {
        try {
            Header header = bitstream.readFrame();
            if (header == null) return false;

            this.currentSampleRate = header.frequency();
            SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);

            this.currentFrameSamples = output.getBuffer();
            this.currentFrameLength = output.getBufferLength();
            this.currentFramePosition = 0;

            bitstream.closeFrame();
            return true;
        } catch (BitstreamException | DecoderException e) {
            return false;
        }
    }

    @Override
    public void rewind() {
        try {
            rawStream.reset();
            this.bitstream = new Bitstream(rawStream);
            this.currentFrameLength = 0;
            this.currentFramePosition = 0;
        } catch (IOException e) {
            throw new IllegalStateException("Could not rewind MP3", e);
        }
    }

    @Override
    public void close() {
        try { bitstream.close(); } catch (BitstreamException ignored) {}
    }
}