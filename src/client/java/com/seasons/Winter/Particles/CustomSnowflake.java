package com.seasons.Winter.Particles;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public class CustomSnowflake extends SpriteBillboardParticle {
    public CustomSnowflake(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, int AgeTicks, SpriteProvider spriteProvider) {
        super(world, x, y, z, vx, vy, vz);
        this.maxAge = AgeTicks;
        this.gravityStrength = 0.05f;
        this.scale = 0.2f;
        this.setSprite(spriteProvider);
    }

    @Override
    public void tick() {
        super.tick();
        BlockPos posBelow = new BlockPos((int)this.x, (int)(this.y - 0.1), (int)this.z); // just cats int
        if (!world.isAir(posBelow)) {
            this.markDead();
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }
}
