package ru.vaneck.sap.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vaneck.sap.lib.AudioCacheManager;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    // Clear and stop all sound instances when the game is closed
    @Inject(method = "close", at=@At("TAIL"))
    public void gameOnClose$SAP(CallbackInfo ci) {
        AudioCacheManager.clearAll();
    }

    // Clear positional sound instances when leaving the world
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At("HEAD"))
    private void onStop$SAP(Screen screen, boolean keepResourcePacks, boolean stopSound, CallbackInfo ci) {
        if (((Minecraft)(Object)this).level != null)
            AudioCacheManager.stopAndClearAllPositionalInstances();
    }
}