package com.seasons.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.seasons.Winter.Winter;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.LightType;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntity.class)
public abstract class FrostbiteMixin {
    @Unique
    private int outdoorTicks = 0;

    @Unique
    private int currFrost = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onPlayerTick(CallbackInfo ci) {
        if (!Winter.IsWinter()) {
            return;
        }
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getWorld().isClient) return;

        int blockLight = player.getWorld().getLightLevel(LightType.BLOCK, player.getBlockPos());

        if (isOutside(player) && hasNoArmor(player)) {
            if (blockLight < 8) {
                outdoorTicks += 2;
            } else if (blockLight < 12) {
                outdoorTicks += 1;
            } else {
                outdoorTicks += 0;
            }
            if (outdoorTicks >= 1200) {
                if (currFrost != 140) {
                    if (blockLight < 12) {
                        currFrost++;
                    }
                    player.setFrozenTicks(Math.min(currFrost, 140));
                }
                if (currFrost == 140) {
                    player.damage(player.getDamageSources().freeze(), 0.5F);
                }
            }
        } else {
            if (currFrost > 0) {
                if (blockLight < 8) {
                    currFrost -= 1;
                } else if (blockLight < 12) {
                    currFrost -= 2;
                } else if (blockLight < 14) {
                    currFrost -= 3;
                } else {
                    currFrost -= 4;
                }
                player.setFrozenTicks(currFrost);
            }
            if (outdoorTicks < 0) {
                if (blockLight < 8) {
                    outdoorTicks -= 1;
                    currFrost -= 1;
                } else if (blockLight < 12) {
                    outdoorTicks -= 2;
                } else if (blockLight < 14) {
                    outdoorTicks -= 3;
                } else {
                    outdoorTicks -= 4;
                }
            }
        }

        if (player.inPowderSnow) {
            currFrost = player.getFrozenTicks();
        }
    }

    @Unique
    private boolean isOutside(PlayerEntity player) {
        return player.getWorld().isSkyVisible(player.getBlockPos());
    }

    @Unique
    private boolean hasNoArmor(PlayerEntity player) {
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

}