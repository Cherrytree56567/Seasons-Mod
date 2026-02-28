package com.ct5.seasons_mod.mixin.client;

import java.util.logging.Logger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.ct5.seasons_mod.SummerClient;
import com.ct5.seasons_mod.WinterClient;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;

@Environment(EnvType.CLIENT)
@Mixin(Player.class)
public abstract class DehydrationMixin {
    @Unique
    private int outdoorTicks = 0;

    @Unique
    private float cTick = 0;

    @Unique
    private float aTick = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onPlayerTick(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (player.level().isClientSide()) return;

        if (!SummerClient.isSummer) {
            if (cTick > 0) {
                cTick -= 2;
                if (cTick < 1) {
                    cTick = 0;
                }
                player.setAirSupply((300 - (int)cTick));
            }
            return;
        }

        int DehydrationValue = (player.getArmorValue() / 2) + 1;

        if (player.level().canSeeSky(player.blockPosition()) && !player.isUnderWater()) {
            outdoorTicks++;
            if (outdoorTicks >= 180 * (12 - DehydrationValue)) {
                if (aTick != 20) {
                    aTick++;
                } else {
                    aTick = 0;
                }
                if (cTick < 300) {
                    if (aTick == 20) {
                        cTick += DehydrationValue;
                        if (cTick > 300) {
                            cTick = 300;
                        }
                    }
                }
                player.setAirSupply((300 - (int)cTick));
                if (cTick == 300) {
                    if (aTick == 20) {
                        player.hurt(player.damageSources().dryOut(), 0.5f);
                    }
                }
            }
        } else {
            if (outdoorTicks > 0) {
                outdoorTicks -= 20;
                if (outdoorTicks < 0) {
                    outdoorTicks = 0;
                }
            }
            if (cTick > 0) {
                cTick -= 1;
                if (cTick < 1) {
                    cTick = 0;
                }
                player.setAirSupply((300 - (int)cTick));
            }
        }
    }
}