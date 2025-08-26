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

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.RenderPhase.*;

public class Winter {
    private static boolean fogEnable = false;
    private static boolean isWinter = false;
    private static List<FogZone> fogZones = new ArrayList<>();

    public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(SeasonsMod.FOG_PACKET_ID, (client, handler, buf, responseSender) -> {
			boolean fogEnabled = buf.readBoolean();
			int fogTimer = buf.readInt();

			client.execute(() -> {
                fogEnable = true;
                MinecraftClient.getInstance().inGameHud.setOverlayMessage(
                    Text.literal("Fog enabled!"), false
                );
            });
		});

		ClientPlayNetworking.registerGlobalReceiver(SeasonsMod.WINTER_PACKET_ID, (client, handler, buf, responseSender) -> {
			boolean winterEnabled = buf.readBoolean();

            client.execute(() -> {
                isWinter = true;
            });
		});

        Random random = new Random();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isWinter) {
                return;
            }
            fogZones.removeIf(zone -> !zone.isActive());

            if (random.nextInt(1000) < 5) {
                ClientWorld world = client.world;
                if (world != null) {
                    double px = client.player.getX() + random.nextInt(50) - 25;
                    double py = client.player.getY();
                    double pz = client.player.getZ() + random.nextInt(50) - 25;
                    fogZones.add(new FogZone(px, py, pz, 16f, 200 + random.nextInt(400)));
                }
            }

            fogZones.forEach(FogZone::tick);
        });

        WorldRenderEvents.START.register(context -> {
            if (!isWinter) {
                return;
            }
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            Camera camera = client.gameRenderer.getCamera();
            ClientWorld world = client.world;

            for (FogZone zone : fogZones) {
                if (zone.isActive()) {
                    if (camera.getPos().distanceTo(new Vec3d(zone.x, zone.y, zone.z)) < zone.radius) {
                        MinecraftClient.getInstance().execute(() -> {
                            MinecraftClient.getInstance().inGameHud.setOverlayMessage(
                                Text.literal("In Fog Zone"), false
                            );
                        });
                        RenderSystem.setShaderFogStart(0.0f);
                        RenderSystem.setShaderFogEnd(1.0f);
                        RenderSystem.setShaderFogColor(1.0f, 1.0f, 1.0f);
                    }
                }
            }
        });
	}
}
