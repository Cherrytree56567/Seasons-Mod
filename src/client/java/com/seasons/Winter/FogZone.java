package com.seasons.Winter;

public class FogZone {
    public double x, y, z;
    public float radius;
    public int timer;

    public FogZone(double x, double y, double z, float radius, int duration) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.timer = duration;
    }

    public boolean isActive() {
        return timer > 0;
    }

    public void tick() {
        if (timer > 0) timer--;
    }
}
