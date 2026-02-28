package com.ct5.seasons_mod;

import net.fabricmc.api.ClientModInitializer;

public class SeasonsModClient implements ClientModInitializer {
	public static void register() {
		AutumnClient.register();
		WinterClient.register();
		SummerClient.register();
		SpringClient.register();
	}

	@Override
	public void onInitializeClient() {
		register();
	}
}