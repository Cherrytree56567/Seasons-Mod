package com.seasons;

import com.seasons.SeasonsMod.FogPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class SeasonsModClient implements ClientModInitializer {
	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(FogPayload.ID, (payload, context) -> {
			boolean fogEnabled = payload.fogEnabled();
			int fogTimer = payload.duration();

			MinecraftClient.getInstance().execute(() -> {
                MinecraftClient.getInstance().inGameHud.setOverlayMessage(
                    Text.literal("Fog enabled for " + fogTimer + " ticks"), false
                );
            });
		});
	}

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		register();
	}
}