package com.ct5.seasons_mod.mixin.client;

import java.util.logging.Logger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.ct5.seasons_mod.WinterClient;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;

@Environment(EnvType.CLIENT)
@Mixin(Player.class)
public abstract class FrostbiteMixin {
    @Unique
    private int outdoorTicks = 0;

    @Unique
    private int currFrost = 0;

    @Unique
    private int cTick = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onPlayerTick(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (player.level().isClientSide()) return;

        int blockLight = player.level().getBrightness(LightLayer.BLOCK, player.blockPosition());

        int FrostValue = (((15 - blockLight) / 5) + ((20 - player.getArmorValue()))) * 10;

        if (!WinterClient.isWinter) {
            if (currFrost > 0) {
                currFrost -= FrostValue;
                outdoorTicks = (int)Math.min(9000, Math.max(0, outdoorTicks + (-(1f / 6f) * blockLight + 1f)));
                player.setTicksFrozen(currFrost);
            }
            return;
        }

        Logger.getAnonymousLogger().info(String.valueOf(outdoorTicks));

        if (player.level().canSeeSky(player.blockPosition())) {
            outdoorTicks += -(1f / 6f) * blockLight + 1f;
            if (outdoorTicks >= (300 * (player.getArmorValue() + 1))) {
                if (currFrost != 140) {
                    if (blockLight < 12) {
                        currFrost = Math.min(140, currFrost + 1);
                    } else {
                        currFrost = Math.max(0, currFrost - 1);
                    }
                }
                player.setTicksFrozen(Math.min(currFrost, 140));
                Logger.getAnonymousLogger().warning(String.valueOf(currFrost));
                if (currFrost == 140) {
                    player.hurt(player.damageSources().freeze(), 1f);
                }
            }
        } else {
            if (cTick == 20) {
                if (currFrost > 0) {
                    currFrost = Math.max(0, currFrost - (blockLight + player.getArmorValue() + 1));
                    player.setTicksFrozen(currFrost);
                }
            }
            if (outdoorTicks > 0) {
                currFrost = Math.max(0, currFrost - (blockLight + player.getArmorValue()));
                outdoorTicks = (int)Math.min(9000, Math.max(0, outdoorTicks + (-(1f / 6f) * blockLight + 1f)));
            }
        }
        cTick++;
        if (cTick == 20) {
            cTick = 0;
        }

        if (player.isInPowderSnow) {
            currFrost = player.getTicksFrozen();
        }
    }
}