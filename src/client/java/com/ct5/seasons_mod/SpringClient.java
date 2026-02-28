package com.ct5.seasons_mod;

import java.util.Random;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

public class SpringClient {
	private static boolean isSpring = false;

	public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(SeasonsMod.SpringS2CPayload.ID, (payload, context) -> {
            boolean springEnabled = payload.isSpring();
            context.client().execute(() -> {
                isSpring = springEnabled;
                Minecraft.getInstance().gui.setOverlayMessage(
                    Component.literal(isSpring ? "Spring has begun!" : "Spring ended!"), false
                );
            });
        });
	}
}