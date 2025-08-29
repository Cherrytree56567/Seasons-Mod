package com.seasons.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntity.class)
public abstract class FrostbiteMixin {
    @Unique
    private int outdoorTicks = 0;

    @Unique
    private int currFrost = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onPlayerTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getWorld().isClient) return;

        if (isOutside(player) && hasNoArmor(player)) {
            outdoorTicks++;
            if (outdoorTicks >= 1200) {
                if (currFrost != 140) {
                    currFrost++;
                    player.setFrozenTicks(Math.min(currFrost, 140));
                }
                if (currFrost == 140) {
                    player.damage(player.getDamageSources().freeze(), 0.5F);
                }
            }
        } else {
            if (currFrost > 0) {
                currFrost--;
                player.setFrozenTicks(currFrost);
            }
            outdoorTicks -= 1;
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