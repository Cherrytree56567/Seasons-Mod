package com.seasons.Autumn;

import java.util.Random;

import org.apache.logging.log4j.core.util.ReflectionUtil;

import com.seasons.SeasonsMod;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

public class Autumn {
    private static boolean isAutumn = false;
    private static float transitionProgress = 0f;

    public static void register() {
        Random random = new Random();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            float speed = 1f / (20f * 60f);
            if (isAutumn && transitionProgress < 1f) {
                transitionProgress += speed;
            } else if (!isAutumn && transitionProgress > 0f) {
                transitionProgress -= speed;
            }

            transitionProgress = Math.max(0f, Math.min(1f, transitionProgress));
        });

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            int InitialRed = (BiomeColors.getFoliageColor(world, pos) >> 16) & 0xFF;
            int InitialGreen = (BiomeColors.getFoliageColor(world, pos) >> 8) & 0xFF;
            int InitialBlue = BiomeColors.getFoliageColor(world, pos) & 0xFF;

            int BrownR = 139;
            int BrownG = 69;
            int BrownB = 19;

            int r = (int)(InitialRed + (BrownR - InitialRed) * transitionProgress);
            int g = (int)(InitialGreen + (BrownG - InitialGreen) * transitionProgress);
            int b = (int)(InitialBlue + (BrownB - InitialBlue) * transitionProgress);
            MinecraftClient client = MinecraftClient.getInstance();
            Camera camera = client.gameRenderer.getCamera();
            MinecraftClient.getInstance().worldRenderer.scheduleBlockRender((int)camera.getPos().x, (int)camera.getPos().y, (int)camera.getPos().z);
            return (r << 16) | (g << 8) | b;
        }, Blocks.OAK_LEAVES, Blocks.BIRCH_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES);

        ClientPlayNetworking.registerGlobalReceiver(SeasonsMod.AUTUMN_PACKET_ID, (client, handler, buf, responseSender) -> {
            boolean autumnEnabled = buf.readBoolean();
            client.execute(() -> {
                isAutumn = autumnEnabled;
                MinecraftClient.getInstance().inGameHud.setOverlayMessage(
                    Text.literal(isAutumn ? "Autumn has begun!" : "Autumn ended!"), false
                );
            });
        });
    }
}
