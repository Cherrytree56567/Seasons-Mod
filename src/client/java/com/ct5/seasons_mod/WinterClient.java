package com.ct5.seasons_mod;

import java.util.Random;
import java.util.logging.Logger;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

public class WinterClient {
	public static boolean isWinter = false;
    public static double transitionProgress = 1f;
    public static float currentFogStartPercent = 1f;
    public static float currentFogEndPercent = 1f;
    private static float targetFogStartPercent = 1f;
    private static float targetFogEndPercent = 1f;
    private static float approach(float current, float target, float step) {
        if (Float.isNaN(current)) return current;
        if (current == target) return current;
        return current < target ? Math.min(target, current + step) : Math.max(target, current - step);
    }

	public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            double speed = 1d / (20 * 5);
            if (!isWinter && transitionProgress > 0f) {
                transitionProgress += speed;
            } else if (isWinter && transitionProgress < 1f) {
                transitionProgress -= speed;
            }

            transitionProgress = Math.max(0f, Math.min(1f, transitionProgress));
            currentFogStartPercent = approach(currentFogStartPercent, targetFogStartPercent, 0.001f);
            currentFogEndPercent = approach(currentFogEndPercent, targetFogEndPercent, 0.001f);
        });

        ClientPlayNetworking.registerGlobalReceiver(SeasonsMod.WinterS2CPayload.ID, (payload, context) -> {
            boolean winterEnabled = payload.isWinter();
            context.client().execute(() -> {
                isWinter = winterEnabled;
                if (isWinter) {
                    currentFogStartPercent = 1f;
                    currentFogEndPercent = 1f;
                    targetFogStartPercent = 0.01f;
                    targetFogEndPercent = 0.05f;
                }
                if (!isWinter) {
                    currentFogStartPercent = 0.01f;
                    currentFogEndPercent = 0.05f;
                    targetFogStartPercent = 1f;
                    targetFogEndPercent = 1f;
                }
                Minecraft.getInstance().gui.setOverlayMessage(
                    Component.literal(isWinter ? "Winter has begun!" : "Winter ended!"), false
                );
            });
        });
	}
}