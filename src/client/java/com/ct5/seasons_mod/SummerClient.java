package com.ct5.seasons_mod;

import java.util.Random;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

public class SummerClient {
	public static boolean isSummer = false;

	public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(SeasonsMod.SummerS2CPayload.ID, (payload, context) -> {
            boolean summerEnabled = payload.isSummer();
            context.client().execute(() -> {
                isSummer = summerEnabled;
                Minecraft.getInstance().gui.setOverlayMessage(
                    Component.literal(isSummer ? "Summer has begun!" : "Summer ended!"), false
                );
            });
        });
	}
}