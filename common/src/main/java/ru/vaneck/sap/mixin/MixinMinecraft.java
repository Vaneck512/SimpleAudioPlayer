package ru.vaneck.sap.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vaneck.sap.lib.AudioCacheManager;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "close", at=@At("TAIL"))
    public void gameOnClose$SAP(CallbackInfo ci) {
        AudioCacheManager.clearAll();
    }
}