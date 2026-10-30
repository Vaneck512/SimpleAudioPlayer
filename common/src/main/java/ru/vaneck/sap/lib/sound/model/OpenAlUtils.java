package ru.vaneck.sap.lib.sound.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import ru.vaneck.sap.mixin.SoundEngineAccessor;
import ru.vaneck.sap.mixin.SoundManagerAccessor;

@SuppressWarnings("ConstantConditions")
public class OpenAlUtils {
    public static boolean isOpenAlReady() {
        if (Minecraft.getInstance() == null) return false;
        SoundManager manager = Minecraft.getInstance().getSoundManager();
        if (manager == null) return false;
        SoundEngine engine = ((SoundManagerAccessor)manager).getSoundEngine$SAP();
        if (engine == null) return false;
        return ((SoundEngineAccessor)engine).isLoaded$SAP();
    }
}
