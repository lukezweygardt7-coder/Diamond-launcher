package net.diamondelantra.demo;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

/**
 * Brings Minecraft's demo limits to Forge:
 *  - only the vanilla demo world (the demo seed) can be played,
 *  - the demo's time limit (5 in-game days, 120500 ticks) ends the session.
 * The client half (title screen, no multiplayer) lives in DemoClient.
 */
@Mod(DemoMod.MOD_ID)
@Mod.EventBusSubscriber(modid = DemoMod.MOD_ID)
public class DemoMod {
	public static final String MOD_ID = "demo";

	/** Same limit as vanilla's DemoMode: 5 in-game days plus a little. */
	public static final long DEMO_LENGTH_TICKS = 120500L;

	public static boolean isDemoSeed(MinecraftServer server) {
		return server.getWorldData().worldGenOptions().seed() == WorldOptions.DEMO_OPTIONS.seed();
	}

	public static boolean isDemoOver(MinecraftServer server) {
		return server.overworld().getGameTime() > DEMO_LENGTH_TICKS;
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		MinecraftServer server = player.getServer();
		if (server == null) return;
		if (!isDemoSeed(server)) {
			player.connection.disconnect(Component.literal("Demo mode: only the demo world can be played."));
		} else if (isDemoOver(server)) {
			player.connection.disconnect(Component.literal("The demo is over. Thanks for playing!"));
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		MinecraftServer server = event.getServer();
		if (server.getTickCount() % 20 != 0) return;
		if (!isDemoOver(server)) return;
		for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {
			player.connection.disconnect(Component.literal("The demo is over. Thanks for playing!"));
		}
	}
}
