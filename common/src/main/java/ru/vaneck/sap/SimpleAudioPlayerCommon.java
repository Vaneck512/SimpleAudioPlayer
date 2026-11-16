package ru.vaneck.sap;

import ru.vaneck.sap.platform.Services;

public class SimpleAudioPlayerCommon {

    public static void init() {
        if (Services.PLATFORM.isModLoaded("sap")) {
            SimpleAudioPlayerConstants.LOG.info("Simple Audio Player is loaded");
        }
    }
}