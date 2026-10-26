package ru.vaneck.sap.lib.sound;

public enum SourceExtension {
    MP3,
    OGG,
    WAV;

    public static SourceExtension parse(String extension) {
        if (extension.contains("mp3") || extension.contains("mpeg"))
            return MP3;
        if (extension.contains("ogg"))
            return OGG;
        if (extension.contains("wav"))
            return WAV;
        try {
            return SourceExtension.valueOf(extension.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
