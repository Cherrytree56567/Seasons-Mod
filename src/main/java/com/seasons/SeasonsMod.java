package com.seasons;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import com.mojang.brigadier.arguments.IntegerArgumentType;

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
    private static boolean fogActive = false;
    private static int fogTimer = 0;

    public enum Season {
		SPRING,
		SUMMER,
		AUTUMN,
		WINTER
	}

    /*
     * Payloads
     */
    public static final Identifier FOG_PACKET_ID = new Identifier("seasonsmod", "fog_event");
    public static final Identifier WINTER_PACKET_ID = new Identifier("seasonsmod", "winter_event");
    public static final Identifier AUTUMN_PACKET_ID = new Identifier("seasonsmod", "autumn_event");

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

    private static void runWinter(ServerWorld world) {
        /*
         * Enable random fog
         * between 2 mins and 1 minecraft day.
         */
        Random random = new Random();

        if (fogActive) {
            fogTimer--;
            if (fogTimer <= 0) {
                fogActive = false;
            }
            return;
        }

        if (!fogActive) {
            int duration = 2400 + random.nextInt(24000 - 2400 + 1);
            int cooldown = 2400 + random.nextInt(4800);

            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBoolean(true);
            buf.writeInt(duration);

            for (ServerPlayerEntity player : world.getPlayers()) {
                ServerPlayNetworking.send(player, FOG_PACKET_ID, buf);
                sendMessage(player, "Fog has been enabled for " + duration + " ticks with a cooldown of " + cooldown + " ticks.");
            }
            fogActive = true;
            fogTimer = duration + cooldown;
        }
    }

    private static void runAutumn(ServerWorld world) {

    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getWorlds().forEach(world -> {
                Season currentSeason = getSeason(world);
                if (currentSeason == Season.WINTER) {
                    runWinter(world);
                }
                if (currentSeason == Season.AUTUMN) {
                    runAutumn(world);
                }
                if (currentSeason.ordinal() != lastSeason) {
                    if (currentSeason == Season.WINTER) {
                        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                        buf.writeBoolean(true);
                        world.getPlayers().forEach(player -> {
                            ServerPlayNetworking.send(player, WINTER_PACKET_ID, buf);
                        });
                        PacketByteBuf Abuf = new PacketByteBuf(Unpooled.buffer());
                        Abuf.writeBoolean(false);
                        world.getPlayers().forEach(player -> {
                            ServerPlayNetworking.send(player, AUTUMN_PACKET_ID, Abuf);
                        });
                    }
                    /*if (currentSeason == Season.AUTUMN) {
                        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                        buf.writeBoolean(true);
                        world.getPlayers().forEach(player -> {
                            ServerPlayNetworking.send(player, AUTUMN_PACKET_ID, buf);
                        });
                    }*/
                    lastSeason = currentSeason.ordinal();
                    world.getPlayers().forEach(player -> {
                        sendMessage(player, "Season has changed! It's now " + getSeasonName(currentSeason));
                    });
                }
            });
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("fog")
                .then(literal("stop")
                    .executes(context -> {
                        SeasonsMod.fogActive = false;
                        fogTimer = 0;
                        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                        buf.writeBoolean(false);
                        buf.writeInt(0);

                        for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) { // ik this is long, but bear with it
                            ServerPlayNetworking.send(player, FOG_PACKET_ID, buf);
                            player.sendMessage(Text.literal("Fog stopped!"), false);
                        }
                        return 1;
                    })
                )
                .then(literal("reset")
                    .executes(context -> {
                        fogTimer = 0;
                        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                        buf.writeBoolean(true);
                        buf.writeInt(1); // other wise whats the point?

                        for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                            ServerPlayNetworking.send(player, FOG_PACKET_ID, buf);
                            player.sendMessage(Text.literal("Fog timer reset!"), false);
                        }
                        return 1;
                    })
                )
                .then(literal("add")
                    .then(argument("ticks", IntegerArgumentType.integer())
                        .executes(context -> {
                            int ticks = IntegerArgumentType.getInteger(context, "ticks");
                            fogTimer += ticks;
                            
                            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                            buf.writeBoolean(true);
                            buf.writeInt(fogTimer);

                            for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                ServerPlayNetworking.send(player, FOG_PACKET_ID, buf);
                                player.sendMessage(Text.literal("Added " + ticks + " ticks to fog timer."), false);
                            }
                            return 1;
                        })
                    )
                )
                .then(literal("set")
                    .then(argument("ticks", IntegerArgumentType.integer())
                        .executes(context -> {
                            int ticks = IntegerArgumentType.getInteger(context, "ticks");
                            fogTimer = ticks;
                            
                            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                            buf.writeBoolean(true);
                            buf.writeInt(fogTimer);

                            for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                ServerPlayNetworking.send(player, FOG_PACKET_ID, buf);
                                player.sendMessage(Text.literal("Fog timer set to tick " + ticks), false);
                            }
                            return 1;
                        })
                    )
                )
            );
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