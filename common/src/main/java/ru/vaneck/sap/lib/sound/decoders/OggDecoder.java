package ru.vaneck.sap.lib.sound.decoders;

import net.minecraft.client.sounds.JOrbisAudioStream;
import ru.vaneck.sap.lib.sound.SAPAudioException;
import ru.vaneck.sap.lib.sound.model.AudioData;

import java.io.IOException;
import java.io.InputStream;

public class OggDecoder extends AudioDecoder {
    @Override
    public AudioData decode(InputStream is) {
        try (JOrbisAudioStream stream = new JOrbisAudioStream(is);) {
            this.decoded = stream.readAll();
            this.audioFormat = stream.getFormat();
            System.out.println(12345);
        } catch (IOException e) {
            throw new SAPAudioException("Failed to get ogg stream, %s".formatted(e.getMessage()));
        }
        return new AudioData(this.decoded, this.audioFormat);
    }
}
