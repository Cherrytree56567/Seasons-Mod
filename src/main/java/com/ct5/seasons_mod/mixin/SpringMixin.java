package com.ct5.seasons_mod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.logging.Logger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.ct5.seasons_mod.SeasonsMod;

@Mixin(CropBlock.class)
public class SpringMixin {
	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	private void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, CallbackInfo callbackInfo) {
        if (SeasonsMod.springEnabled) {
            CropBlock crop = (CropBlock)(Object)this;

            int age = blockState.getValue(CropBlock.AGE);

            if (serverLevel.getRawBrightness(blockPos, 0) >= 9) {
                if (age < crop.getMaxAge()) {
                    BlockState newState = blockState.setValue(CropBlock.AGE, age + 1);
                    serverLevel.setBlock(blockPos, crop.getStateForAge(age + 1), 2);
                    
                }
            }
            callbackInfo.cancel();
        }
	}
}