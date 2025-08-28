package com.seasons.Winter.Particles;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class CustomSnowflakeFactory implements ParticleFactory<DefaultParticleType> {
    private final SpriteProvider spriteProvider;

    public CustomSnowflakeFactory(SpriteProvider spriteProvider) {
        this.spriteProvider = spriteProvider;
    }

    @Override
    public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double dx, double dy, double dz) {
        return new CustomSnowflake(world, x, y, z, dx, dy, dz, Integer.MAX_VALUE, spriteProvider);
    }
}
