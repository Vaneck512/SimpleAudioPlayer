package ru.vaneck.sap.lib.sound;

import javazoom.jl.decoder.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.overviewproject.mime_types.MimeTypeDetector;
import ru.vaneck.sap.lib.sound.model.AudioData;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;

public class AudioUtils {
    public static AudioData decodeStream(InputStream is, SourceExtension extension) throws Exception {
        AudioData data = switch (extension) {
            case OGG -> {
                try(JOrbisAudioStream as = new JOrbisAudioStream(is)) {
                    yield new AudioData(as.readAll(), as.getFormat());
                }
            }
            case WAV -> {
                try (AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(is))) {
                    byte[] array = stream.readAllBytes();
                    //It's important to allocate the buffer, because otherwise OpenAL can't read it
                    ByteBuffer buffer = ByteBuffer.allocateDirect(array.length);
                    buffer.put(array);
                    buffer.flip();
                    yield new AudioData(buffer, stream.getFormat());
                }
            }
            case MP3 -> getMp3Stream(is);
        };
        is.close();
        return data;
    }

    static InputStream openStream(String source, AudioType type) throws IOException {
        return switch (type) {
            case URL -> {
                URI uri = URI.create(source);
                if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
                    throw new SAPAudioException("Only HTTP/HTTPS URLs are supported: " + source);
                }
                HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
                connection.setConnectTimeout(10_000);
                connection.setReadTimeout(15_000);
                connection.setRequestProperty("User-Agent", "SimpleAudioPlayer");
                yield connection.getInputStream();
            }
            case FILE -> new BufferedInputStream(new FileInputStream(source));
            case LOCATION -> {
                Identifier id = Identifier.tryParse(source);
                if (id == null) throw new SAPAudioException("Invalid resource identifier: " + source);
                Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(id);
                if (resource.isEmpty()) throw new SAPAudioException("Audio resource not found: " + source);
                yield resource.get().open();
            }
        };
    }

    public static AudioData getMp3Stream(InputStream is) throws BitstreamException {

        Bitstream bitstream = new Bitstream(is);
        Decoder decoder = new Decoder();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        AudioFormat audioFormat = null;
        try {
            Header frameHeader;

            while ((frameHeader = bitstream.readFrame()) != null) {
                SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);

                // short (PCM 16-bit)
                short[] pcmData = output.getBuffer();
                int length = output.getBufferLength();

                for (int i = 0; i < length; i++) {
                    short sample = pcmData[i];
                    outStream.write(sample & 0xFF);
                    outStream.write((sample >> 8) & 0xFF);
                }

                audioFormat = new AudioFormat(
                        output.getSampleFrequency(),
                        16,
                        output.getChannelCount(),
                        true,
                        false
                );

                bitstream.closeFrame();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            bitstream.close();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(outStream.toByteArray().length).order(ByteOrder.LITTLE_ENDIAN).put(outStream.toByteArray()).flip();

        return new AudioData(byteBuffer, audioFormat);
    }

    public static ByteBuffer convertBufferToMono(ByteBuffer byteBuffer, AudioFormat format) throws SAPAudioException {
        if (format.getChannels() != 2) return byteBuffer;

        ByteBuffer copy = byteBuffer.duplicate().order(byteBuffer.order());
        copy.rewind();

        ByteBuffer mono;

        if (format.getSampleSizeInBits() == 16) {
            int samples = copy.remaining() / 4;

            mono = ByteBuffer.allocateDirect(samples*2).order(byteBuffer.order());

            for (int i = 0; i < samples; i++) {
                short left = copy.getShort();
                short right = copy.getShort();
                mono.putShort((short) ((left + right) /2));
            }
        } else if (format.getSampleSizeInBits() == 8) {
            boolean isUnsigned = format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED;
            int samples = copy.remaining() / 2;
            mono = ByteBuffer.allocateDirect(samples).order(byteBuffer.order());
            for (int i = 0; i < samples; i++) {
                int left;
                int right;
                if (isUnsigned) {
                    left = copy.get() & 0xFF;
                    right = copy.get() & 0xFF;
                } else {
                    left = copy.get();
                    right = copy.get();
                }
                mono.put((byte) ((left + right) / 2));
            }
        } else {
            throw new SAPAudioException("Mono conversation supports only 8/16 bit PCM. Current is: " + format.getSampleSizeInBits());
        }
        mono.flip();
        return mono;
    }

    public static SourceExtension parseStreamExtension(BufferedInputStream inputStream) {
        MimeTypeDetector detector = new MimeTypeDetector();
        inputStream.mark(8192);
        String mimeType;

        try {
            mimeType = detector.detectMimeType("", inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to detect mimeType", e);
        }

        try {
            inputStream.reset();
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset InputStream.", e);
        }

        String extension = mimeType.contains("/") ? mimeType.split("/")[1] : "";
        extension = extension.replace("mpeg", "mp3");
        return SourceExtension.parse(extension);
    }
}
