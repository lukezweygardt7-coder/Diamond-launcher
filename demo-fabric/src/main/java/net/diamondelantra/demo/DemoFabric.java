package net.diamondelantra.demo;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;

/**
 * Demo limits for Fabric, matching the Forge demo.jar:
 *  - only the vanilla demo world (the demo seed),
 *  - one in-game day (about 20 minutes) ends the session, with a reminder every minute,
 *  - chat is disabled. The client half (no multiplayer) is DemoFabricClient.
 */
public class DemoFabric implements ModInitializer {
	/** Same seed vanilla's demo world uses. */
	public static final long DEMO_SEED = "North Carolina".hashCode();
	public static final long DEMO_LENGTH_TICKS = 24000L;

	private static boolean isOver(MinecraftServer server) {
		return server.getOverworld().getTime() > DEMO_LENGTH_TICKS;
	}

	@Override
	public void onInitialize() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (server.getSaveProperties().getGeneratorOptions().getSeed() != DEMO_SEED) {
				handler.disconnect(Text.literal("Demo mode: only the demo world can be played."));
			} else if (isOver(server)) {
				handler.disconnect(Text.literal("The demo is over. Thanks for playing!"));
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			int ticks = server.getTicks();
			if (ticks % 20 != 0) return;

			if (isOver(server)) {
				for (ServerPlayerEntity player : new ArrayList<>(server.getPlayerManager().getPlayerList())) {
					player.networkHandler.disconnect(Text.literal("The demo is over. Thanks for playing!"));
				}
				return;
			}
			if (ticks % 1200 == 0) {
				long minutes = (DEMO_LENGTH_TICKS - server.getOverworld().getTime()) / 1200L + 1;
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					player.sendMessage(Text.literal("Demo: " + minutes + " min left. Buy Minecraft: Java Edition to play for real."), true);
				}
			}
		});

		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
			sender.sendMessage(Text.literal("Chat is disabled in the demo."), true);
			return false;
		});
	}
}
