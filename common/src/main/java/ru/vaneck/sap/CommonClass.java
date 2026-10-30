package ru.vaneck.sap;

import ru.vaneck.sap.platform.Services;

public class CommonClass {

    public static void init() {
        if (Services.PLATFORM.isModLoaded("sap")) {
            Constants.LOG.info("Simple Audio Player is loaded");
        }
    }
}