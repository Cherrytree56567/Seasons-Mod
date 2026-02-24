package com.ct5.seasons_mod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.io.IOException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ct5.seasons_mod.SeasonsMod.AutumnS2CPayload;
import com.ct5.seasons_mod.SeasonsMod.Season;
import com.mojang.brigadier.arguments.IntegerArgumentType;

public class SeasonsMod implements ModInitializer {
	public static final String MOD_ID = "seasons-mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public record AutumnS2CPayload(boolean isAutumn) implements CustomPacketPayload {
		public static final Identifier AUTUMN_PACKET_ID = Identifier.fromNamespaceAndPath(MOD_ID, "autumn_event");
		public static final CustomPacketPayload.Type<AutumnS2CPayload> ID = new CustomPacketPayload.Type<>(AUTUMN_PACKET_ID);
		public static final StreamCodec<RegistryFriendlyByteBuf, AutumnS2CPayload> CODEC = 
			StreamCodec.composite(net.minecraft.network.codec.ByteBufCodecs.BOOL, AutumnS2CPayload::isAutumn, AutumnS2CPayload::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

	public record WinterS2CPayload(boolean isWinter) implements CustomPacketPayload {
		public static final Identifier WINTER_PACKET_ID = Identifier.fromNamespaceAndPath(MOD_ID, "winter_event");
		public static final CustomPacketPayload.Type<WinterS2CPayload> ID = new CustomPacketPayload.Type<>(WINTER_PACKET_ID);
		public static final StreamCodec<RegistryFriendlyByteBuf, WinterS2CPayload> CODEC = 
			StreamCodec.composite(net.minecraft.network.codec.ByteBufCodecs.BOOL, WinterS2CPayload::isWinter, WinterS2CPayload::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

	/*
     * Season Variables
     */
    private static int lastSeason = 0;
	private static Boolean rainEnabled = false;

    public enum Season {
		NONE,
		SPRING,
		SUMMER,
		AUTUMN,
		WINTER
	}
	
	/*
     * For Debugging
     */
	public static void sendMessage(String message) {
        LOGGER.info(message);
    }

	public static Season getSeason(ServerLevel level) {
		long time = level.getDayTime();
        long daysPerSeason = 24000 * 7;
        int season = (int)((time / daysPerSeason) % 4) + 1;
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

	private static void runWinter(ServerLevel level) {
		RandomSource random = level.getRandom();
		int n = random.nextInt(5000);
		int x = random.nextInt(5000);
		float rainLevel = random.nextFloat();
		if (n == x) {
			if (rainEnabled) {
				level.setRainLevel(rainLevel);
				rainEnabled = false;
			} else {
				level.setRainLevel(0f);
				rainEnabled = true;
			}
		}
	}

	private static void runAutumn(ServerLevel level) {
	}

	private static void runSpring(ServerLevel level) {
	}

	private static void runSummer(ServerLevel level) {
	}

	public static void register() {
		PayloadTypeRegistry.playS2C().register(AutumnS2CPayload.ID, AutumnS2CPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(WinterS2CPayload.ID, WinterS2CPayload.CODEC);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
			server.getAllLevels().forEach(level -> {
				Season currentSeason = getSeason(level);
				if (currentSeason == Season.WINTER) {
					runWinter(level);
				}
				if (currentSeason == Season.AUTUMN) {
					runAutumn(level);
				}
				if (currentSeason == Season.SPRING) {
					runSpring(level);
				}
				if (currentSeason == Season.SUMMER) {
					runSummer(level);
				}
				if (currentSeason.ordinal() != lastSeason) {
					sendMessage("Season has changed! It's now " + getSeasonName(currentSeason));
					if (Season.values()[lastSeason] == Season.WINTER) {
						level.setRainLevel(0f);
						WinterS2CPayload payload = new WinterS2CPayload(false);

						for (ServerPlayer player : PlayerLookup.world((ServerLevel) level)) {
							ServerPlayNetworking.send(player, payload);
						}
					}
					if (Season.values()[lastSeason] == Season.AUTUMN) {
						AutumnS2CPayload payload = new AutumnS2CPayload(false);

						for (ServerPlayer player : PlayerLookup.world((ServerLevel) level)) {
							ServerPlayNetworking.send(player, payload);
						}
					}
					if (Season.values()[lastSeason] == Season.SPRING) {
					}
					if (Season.values()[lastSeason] == Season.SUMMER) {
					}
					lastSeason = currentSeason.ordinal();
					if (currentSeason == Season.AUTUMN) {
						AutumnS2CPayload payload = new AutumnS2CPayload(true);

						for (ServerPlayer player : PlayerLookup.world((ServerLevel) level)) {
							ServerPlayNetworking.send(player, payload);
						}
					}
					if (currentSeason == Season.WINTER) {
						WinterS2CPayload payload = new WinterS2CPayload(true);

						for (ServerPlayer player : PlayerLookup.world((ServerLevel) level)) {
							ServerPlayNetworking.send(player, payload);
						}
					}
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