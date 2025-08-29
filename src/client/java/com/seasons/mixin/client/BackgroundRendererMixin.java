package com.seasons.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seasons.Winter.FogState;

/*
 * Inspired by @Steveplays28:
 * https://github.com/Steveplays28/biomefog/blob/main/src/main/java/io/github/steveplays28/biomefog/mixin/BackgroundRendererMixin.java
 */
@Environment(EnvType.CLIENT)
@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
	@Unique
	private static Boolean fogEnabled = true;

	@Inject(method = "applyFog", at = @At("TAIL"))
	private static void WinterFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo info) {
		var world = MinecraftClient.getInstance().world;
		var player = MinecraftClient.getInstance().player;
		var cameraSubmersionType = camera.getSubmersionType();
		if (world == null || player == null || !(cameraSubmersionType.equals(
				CameraSubmersionType.NONE) || cameraSubmersionType.equals(
				CameraSubmersionType.WATER))) {
			fogEnabled = false;
			return;
		}

		fogEnabled = true;

		Float fogStartAddition;
		Float fogEndAddition;
		Vec3d fogColor;
		Vec3d defaultFogColor;

		// Check if it is night
		if (world.getTimeOfDay() >= 13000 && world.getTimeOfDay() <= 23000) {
			fogStartAddition = 100f;
			fogEndAddition = 150f;
			fogColor = new Vec3d(0.1f, 0.1f, 0.1f);
			defaultFogColor = new Vec3d(0f, 0f, 0f);
		} else {
			fogStartAddition = 0f;
			fogEndAddition = 1f;
			fogColor = new Vec3d(0.96f, 0.98f, 0.94f);
			defaultFogColor = new Vec3d(0.68f, 0.83f, 1f);
		}

		fogStartAddition = MathHelper.lerp(0.001f, 0f, fogStartAddition);
		fogEndAddition = MathHelper.lerp(0.001f, 0f, fogEndAddition);

		fogColor = fogColor.lerp(fogColor, 0.001f);

		if (cameraSubmersionType.equals(CameraSubmersionType.WATER)) {
			RenderSystem.setShaderFogStart(-8f);

			var fogEnd = 96.0f;
			fogEnd *= Math.max(0.25f, player.getUnderwaterVisibility());
			if (fogEnd > viewDistance) {
				fogEnd = viewDistance;
			}

			var currentBiome = player.getWorld().getBiome(player.getBlockPos());
			if (currentBiome.isIn(BiomeTags.HAS_CLOSER_WATER_FOG)) {
				fogEnd *= 0.85f;
			}

			RenderSystem.setShaderFogEnd(fogEnd);
		} else {
			RenderSystem.setShaderFogStart(MathHelper.lerp(FogState.getFogEnable(), viewDistance - (MathHelper.clamp(64.0f, viewDistance / 10.0f, 4.0f)), 0f + 0f));
			RenderSystem.setShaderFogEnd(MathHelper.lerp(FogState.getFogEnable(), viewDistance, viewDistance / 3 + 0f));
			RenderSystem.setShaderFogColor((float)defaultFogColor.x, (float)defaultFogColor.y, (float)defaultFogColor.z, 1f);
		}
	}
}