package ru.vaneck.sap.lib.sound.model;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;

public record AudioData(ByteBuffer data, AudioFormat format) {
}
