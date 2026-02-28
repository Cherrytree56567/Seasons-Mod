package com.ct5.seasons_mod.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.ct5.seasons_mod.WinterClient;

/*
 * Modified version of IMB11's Fog Mod:
 * https://github.com/IMB11/Fog/blob/master/src/main/java/dev/imb11/fog/mixin/client/rendering/FogRendererMixin.java
 */
@Environment(EnvType.CLIENT)
@Mixin(FogRenderer.class)
public class FogRendererMixin {

	@Redirect(method = "setupFog", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/fog/FogData;renderDistanceStart:F", opcode = Opcodes.PUTFIELD))
    private void modifyRenderDistanceStart(FogData data, float renderDistanceStart) {
		  data.renderDistanceStart = (float)(renderDistanceStart * WinterClient.transitionProgress + 10f * (1f - WinterClient.transitionProgress));
    }

    @Redirect(method = "setupFog", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/fog/FogData;renderDistanceEnd:F", opcode = Opcodes.PUTFIELD))
    private void modifyRenderDistanceEnd(FogData data, float renderDistanceEnd) {
		  data.renderDistanceEnd = (float)(renderDistanceEnd * WinterClient.transitionProgress + 15f * (1f - WinterClient.transitionProgress));
    }
}