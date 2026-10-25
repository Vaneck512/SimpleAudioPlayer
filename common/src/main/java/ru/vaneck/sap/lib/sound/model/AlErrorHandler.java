package ru.vaneck.sap.lib.sound.model;

import org.lwjgl.openal.AL10;

public class AlErrorHandler {
    public static void checkOpenAlError() throws ALException {
        int error = AL10.alGetError();
        if (error != 0) throw new ALException(AL10.alGetString(error));
    }
}
