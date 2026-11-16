package ru.vaneck.sap.lib.sound;

public class SAPAudioException extends RuntimeException {
    public SAPAudioException(String message) {
        super(message);
    }
    public SAPAudioException(String message, Throwable cause) {
        super(message, cause);
    }
}
