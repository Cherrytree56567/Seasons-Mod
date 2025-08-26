package com.seasons.Winter;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.seasons.SeasonsMod.FogPayload;
import com.seasons.SeasonsMod.WinterPayload;

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

public class Winter {
    private static boolean fogEnable;
    private static boolean isWinter;
    private static List<FogZone> fogZones = new ArrayList<>();

    public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(FogPayload.ID, (payload, context) -> {
			boolean fogEnabled = payload.fogEnabled();
			int fogTimer = payload.duration();

			MinecraftClient.getInstance().execute(() -> {
                MinecraftClient.getInstance().inGameHud.setOverlayMessage(
                    Text.literal("Fog enabled!"), false
                );
            });

			fogEnable = true;
		});

		ClientPlayNetworking.registerGlobalReceiver(WinterPayload.ID, (payload, context) -> {
			boolean winterEnabled = payload.winterEnabled();

            isWinter = true;
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
                        float fogStart = 0.0f; // computed
                        float fogEnd = 16.0f; // computed
                        float r = 1.0f; // computed
                        float g = 1.0f; // computed
                        float b = 1.0f; // computed

                        // Create buffer
                        FloatBuffer buffer = BufferUtils.createFloatBuffer(6);
                        buffer.put(fogStart).put(fogEnd).put(r).put(g).put(b).put(1.0f).flip();

                        // Upload to GPU
                        GpuBuffer gpuBuffer = GpuBuffer.create(buffer, true); // true = dynamic
                        RenderSystem.setShaderFog(gpuBuffer);
                    }
                }
            }
        });
	}
}
