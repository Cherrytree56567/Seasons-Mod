package com.seasons.Winter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seasons.SeasonsMod;
import com.seasons.mixin.client.BackgroundRendererMixin;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome.Precipitation;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.client.render.RenderPhase.*;

public class Winter {
    private static boolean fogEnable = false;
    private static boolean isWinter = false;
    private static long fogTimer = 0;

    public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(SeasonsMod.FOG_PACKET_ID, (client, handler, buf, responseSender) -> {
			boolean fogEnabled = buf.readBoolean();
			fogTimer = buf.readInt();
            fogEnable = fogEnabled;
		});

		ClientPlayNetworking.registerGlobalReceiver(SeasonsMod.WINTER_PACKET_ID, (client, handler, buf, responseSender) -> {
			boolean winterEnabled = buf.readBoolean();

            client.execute(() -> {
                isWinter = true;
                MinecraftClient.getInstance().inGameHud.setOverlayMessage(
                    Text.literal("Winter!"), false
                );
            });
		});

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isWinter) {
                return;
            }

            if (fogTimer == 0 && !fogEnable) {
                fogEnable = false;
            }

            if (fogEnable) {
                FogState.setFogEnable(FogState.getFogEnable() + 0.01f);
            } else {
                FogState.setFogEnable(FogState.getFogEnable() - 0.01f);
            }
            fogTimer -= 1;
        });
	}
}
