package com.ct5.seasons_mod;

import java.util.Random;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

public class AutumnClient {
	private static boolean isAutumn = false;
    private static float transitionProgress = 0f;

	public static void register() {
		Random random = new Random();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            float speed = 1f / (20f * 60f);
            boolean isChanged = false;
            int tic = 0;
            if (isAutumn && transitionProgress < 1f) {
                transitionProgress += speed;
                isChanged = true;
            } else if (!isAutumn && transitionProgress > 0f) {
                transitionProgress -= speed;
                isChanged = true;
            }
            tic++;

            transitionProgress = Math.max(0f, Math.min(1f, transitionProgress));

            if (isChanged && client.level != null) {
                if (tic == 20) {
                    tic = 0;
                    isChanged = false;
                    client.levelRenderer.allChanged();
                }
            }
        });

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            int InitialRed = (BiomeColors.getAverageFoliageColor(world, pos) >> 16) & 0xFF;
            int InitialGreen = (BiomeColors.getAverageFoliageColor(world, pos) >> 8) & 0xFF;
            int InitialBlue = BiomeColors.getAverageFoliageColor(world, pos) & 0xFF;

            int BrownR = 139;
            int BrownG = 69;
            int BrownB = 19;

            int r = (int)(InitialRed + (BrownR - InitialRed) * transitionProgress);
            int g = (int)(InitialGreen + (BrownG - InitialGreen) * transitionProgress);
            int b = (int)(InitialBlue + (BrownB - InitialBlue) * transitionProgress);
            return (r << 16) | (g << 8) | b;
        }, Blocks.OAK_LEAVES, Blocks.BIRCH_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES);

        ClientPlayNetworking.registerGlobalReceiver(SeasonsMod.AutumnS2CPayload.ID, (payload, context) -> {
            boolean autumnEnabled = payload.isAutumn();
            context.client().execute(() -> {
                isAutumn = autumnEnabled;
                Minecraft.getInstance().gui.setOverlayMessage(
                    Component.literal(isAutumn ? "Autumn has begun!" : "Autumn ended!"), false
                );
            });
        });
	}
}