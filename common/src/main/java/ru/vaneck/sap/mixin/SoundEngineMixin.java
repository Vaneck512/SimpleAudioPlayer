package ru.vaneck.sap.mixin;

import net.minecraft.client.Options;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vaneck.sap.lib.sound.MinecraftSettingsListeners;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @Shadow
    private boolean loaded;
    @Shadow @Final
    private Options options;

    // Both option sliders and temporary category gains reach this method; sliders bypass updateCategoryVolume.
    @Inject(method = "refreshCategoryVolume", at = @At("RETURN"))
    private void onRefresh$SAP(SoundSource source, CallbackInfo ci) {
        if (this.loaded) {
            //Notify all volume listeners of MinecraftSoundSettingsObserver
            MinecraftSettingsListeners.getVolumeListeners().forEach(soundSourceFloatBiConsumer -> soundSourceFloatBiConsumer.accept(source, this.options.getFinalSoundSourceVolume(source)));
        }
    }
}
