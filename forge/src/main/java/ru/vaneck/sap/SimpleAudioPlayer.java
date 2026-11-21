package ru.vaneck.sap;

import net.minecraftforge.fml.common.Mod;
import ru.vaneck.sap.lib.SAP;
import ru.vaneck.sap.lib.sound.AudioType;

@Mod(SimpleAudioPlayerConstants.MOD_ID)
public class SimpleAudioPlayer {

    public SimpleAudioPlayer() {

        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
//        Constants.LOG.info("Hello Forge world!");
        SimpleAudioPlayerCommon.init();
    }
}