package com.ct5.seasons_mod.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.ct5.seasons_mod.WinterClient;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;

/*
 * Modified version of IMB11's Fog Mod:
 * https://github.com/IMB11/Fog/blob/master/src/main/java/dev/imb11/fog/mixin/client/rendering/BackgroundRendererMixin.java
 */
@Mixin(AtmosphericFogEnvironment.class)
public class AtmosphericFogEnvironmentMixin {
    @Inject(at = @At("TAIL"), method = "setupFog(Lnet/minecraft/client/renderer/fog/FogData;Lnet/minecraft/client/Camera;Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/DeltaTracker;)V")
    public void tailSetupFog(FogData fogData, Camera camera, ClientLevel clientLevel, float viewDistance, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        fogData.environmentalStart = Math.max(240, viewDistance) * WinterClient.currentFogStartPercent;
        fogData.environmentalEnd = Math.max(240, viewDistance) * WinterClient.currentFogEndPercent;
        fogData.cloudEnd = fogData.environmentalEnd;
        fogData.skyEnd = fogData.environmentalEnd;
    }
}