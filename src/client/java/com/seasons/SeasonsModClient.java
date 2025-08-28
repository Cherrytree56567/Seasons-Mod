package com.seasons;

import com.seasons.Autumn.Autumn;
import com.seasons.Winter.Winter;

import net.fabricmc.api.ClientModInitializer;

public class SeasonsModClient implements ClientModInitializer {
	public static void register() {
		Winter.register();
		//Autumn.register();
	}

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		register();
	}
}