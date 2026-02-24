package com.ct5.seasons_mod.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.ct5.seasons_mod.WinterClient;

import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
@Mixin(Biome.class)
public class SnowMixin {
    @Inject(method = "coldEnoughToSnow", at = @At("HEAD"), cancellable = true)
    private void alwaysSnow(BlockPos pos, int i, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(WinterClient.isWinter);
    }
}
