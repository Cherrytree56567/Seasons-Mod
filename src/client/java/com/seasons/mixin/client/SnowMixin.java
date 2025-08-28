package com.seasons.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.seasons.Winter.Winter;
import com.seasons.Winter.Particles.CustomSnowflake;
import com.seasons.Winter.Particles.CustomSnowflakeFactory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
@Mixin(ClientWorld.class)
public class SnowMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onWorldTick(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        //if (!Winter.IsWinter()) return;

        BlockPos pos = client.player.getBlockPos();
        Biome biome = client.world.getBiome(pos).value();

        int snow_particles = 8;

        if (biome.isCold(pos)) {
            snow_particles = 32;
        }

        for (int i = 0; i < snow_particles; i++) {
            double x = client.player.getX() + (client.world.random.nextDouble() - 0.5) * 32;
            double y = client.player.getY() + 10 + client.world.random.nextDouble() * 3;
            double z = client.player.getZ() + (client.world.random.nextDouble() - 0.5) * 32;

            double velX = (client.world.random.nextDouble() - 0.5) * 0.05;
            double velY = -0.05 - client.world.random.nextDouble() * 0.05;
            double velZ = (client.world.random.nextDouble() - 0.5) * 0.05;

            client.particleManager.addParticle(Winter.snowFact.createParticle(ParticleTypes.SNOWFLAKE, client.world, x, y, z, velX, velY, velZ));
        }
    }
}
