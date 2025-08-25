package com.seasons;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;

public class SeasonsMod implements ModInitializer {
	public static final String MOD_ID = "seasons-mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /*
     * Season Variables
     */
    private static int lastSeason = -1;

    public enum Season {
		SPRING,
		SUMMER,
		AUTUMN,
		WINTER
	}

    /*
     * Season Client-Side Packets
     */
    public static final Identifier FOG_PACKET = Identifier.tryParse("seasonsmod:fog_event");

    /*
     * For Debugging
     */
	public static void sendMessage(ServerPlayerEntity player, String message) {
        player.sendMessage(Text.literal(message), false);
    }

	public static Season getSeason(ServerWorld world) {
		long time = world.getTimeOfDay();
        long ticksPerSeason = 24000 * 7;
        int season = (int)((time / ticksPerSeason) % 4);
		return Season.values()[season];
	}

	public static String getSeasonName(Season season) {
        return switch (season) {
            case SPRING -> "Spring";
            case SUMMER -> "Summer";
            case AUTUMN -> "Autumn";
            case WINTER -> "Winter";
            default -> "Unknown";
        };
    }

    public record FogPayload(boolean fogEnabled, int duration) implements CustomPayload {
        public static final Identifier FOG_PACKET_ID = Identifier.of("seasonsmod", "fog_event");
        public static final CustomPayload.Id<FogPayload> ID = new CustomPayload.Id<>(FOG_PACKET_ID);

        public static final PacketCodec<RegistryByteBuf, FogPayload> CODEC =
            PacketCodec.tuple(
                PacketCodecs.BOOLEAN, FogPayload::fogEnabled,
                PacketCodecs.INTEGER, FogPayload::duration,
                FogPayload::new
            );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    private static void runWinter(ServerWorld world) {
        /*
         * Enable random fog
         * between 2 mins and 1 minecraft day.
         */
        Random random = new Random();

        int duration = 2400 + random.nextInt(24000 - 2400 + 1);

        if (random.nextInt(20) == 0) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBoolean(true); // Enable Fog
            buf.writeInt(duration); // How long to enable Fog

            FogPayload payload = new FogPayload(true, duration);

            for (ServerPlayerEntity player : world.getPlayers()) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(FogPayload.ID, FogPayload.CODEC);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getWorlds().forEach(world -> {
                Season currentSeason = getSeason(world);
                if (currentSeason == Season.WINTER) {
                    runWinter(world);
                }
                if (currentSeason.ordinal() != lastSeason) {
                    lastSeason = currentSeason.ordinal();
                    world.getPlayers().forEach(player -> {
                        sendMessage(player, "Season has changed! It's now " + getSeasonName(currentSeason));
                    });
                }
            });
        });
    }

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
        register();
	}
}