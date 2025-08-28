package com.seasons.Winter;

import com.seasons.SeasonsMod;
import com.seasons.Winter.Particles.CustomSnowflakeFactory;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;

public class Winter {
    private static boolean fogEnable = false;
    private static boolean isWinter = false;
    private static long fogTimer = 0;
    public static CustomSnowflakeFactory snowFact;

    public static boolean IsWinter() {
        return isWinter;
    }

    public static void register() {
        ParticleFactoryRegistry.getInstance().register(ParticleTypes.SNOWFLAKE, (spriteProvider) -> {
            snowFact = new CustomSnowflakeFactory(spriteProvider);
            return snowFact;
        });

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
