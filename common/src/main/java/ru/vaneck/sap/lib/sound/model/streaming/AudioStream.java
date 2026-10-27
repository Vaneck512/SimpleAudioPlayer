package ru.vaneck.sap.lib.sound.model.streaming;

import java.nio.ShortBuffer;

public interface AudioStream extends AutoCloseable {
    int channels();
    int sampleRate();
    int read(ShortBuffer pcm);
    boolean isMono();
    void rewind();
}
