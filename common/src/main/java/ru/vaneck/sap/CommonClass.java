package ru.vaneck.sap;

import ru.vaneck.sap.lib.SAP;
import ru.vaneck.sap.lib.sound.AudioType;
import ru.vaneck.sap.platform.Services;

public class CommonClass {

    public static void init() {
        if (Services.PLATFORM.isModLoaded("sap")) {
            Constants.LOG.info("Simple Audio Player is loaded");
        }

        SAP.play("https://www.newgrounds.com/audio/download/1397300", AudioType.URL, settings -> {

        });
//        SAP.play("C:\\Users\\Ivan\\Music\\music\\Tryhardninja-Follow-Me.ogg", AudioType.FILE, 0.1F);

//        new Thread(() -> {
//            while (!OpenAlUtils.isOpenAlReady())
//                Thread.onSpinWait();
//            AudioFactory.playOnce(
//                    "https://www.newgrounds.com/audio/download/1397300",
//                    AudioType.URL,
//                    false,
//                    true,
//                    instance -> {
//                        instance.setVolume(0.25F);
//                    }
//            );
//        }).start();
    }
}