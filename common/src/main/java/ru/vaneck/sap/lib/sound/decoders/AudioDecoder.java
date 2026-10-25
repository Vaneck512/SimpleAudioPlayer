package ru.vaneck.sap.lib.sound.decoders;

import ru.vaneck.sap.lib.sound.model.AudioData;

import javax.sound.sampled.AudioFormat;
import java.io.InputStream;
import java.nio.ByteBuffer;

public abstract class AudioDecoder {
    ByteBuffer decoded;
    AudioFormat audioFormat;

    public abstract AudioData decode(InputStream is);
}
