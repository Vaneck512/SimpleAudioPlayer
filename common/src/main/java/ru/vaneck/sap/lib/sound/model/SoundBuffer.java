package ru.vaneck.sap.lib.sound.model;

import org.lwjgl.system.*;
import ru.vaneck.sap.Constants;

import javax.sound.sampled.AudioFormat;
import java.nio.*;

import static org.lwjgl.openal.AL10.*;

public class SoundBuffer implements AudioInstance {
    private final int bufferId;

    private final ByteBuffer pcm;
    private boolean closed;

    public SoundBuffer(ByteBuffer data, AudioFormat format) {
        this.bufferId = alGenBuffers();
        pcm = data;
        try {
            alBufferData(bufferId, format.getChannels() == 2 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16, pcm, (int) format.getSampleRate());
            AlErrorHandler.checkOpenAlError();
        } catch (ALException e) {
            alDeleteBuffers(bufferId);
            Constants.LOG.error("Failed to create SoundBuffer, {}", e.getMessage());
        }
    }

    public void cleanup() {
        alDeleteBuffers(this.bufferId);
        if (pcm != null) {
            MemoryUtil.memFree(pcm);
        }
    }

    @Override
    public void close() {
        cleanup();
        this.closed = true;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public int getBufferId() {
        return this.bufferId;
    }

}