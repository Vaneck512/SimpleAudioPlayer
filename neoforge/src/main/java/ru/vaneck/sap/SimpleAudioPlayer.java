package ru.vaneck.sap;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class SimpleAudioPlayer {

    public SimpleAudioPlayer(IEventBus eventBus) {
        CommonClass.init();
    }
}