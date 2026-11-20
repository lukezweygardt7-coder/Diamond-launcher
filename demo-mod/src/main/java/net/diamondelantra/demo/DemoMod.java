package net.diamondelantra.demo;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

/**
 * Brings Minecraft's demo limits to Forge:
 *  - only the vanilla demo world (the demo seed) can be played,
 *  - a strict time limit (one in-game day) ends the session, with a reminder every minute,
 *  - chat and commands are disabled.
 * The client half (title screen, no multiplayer) lives in DemoClient.
 */
@Mod(DemoMod.MOD_ID)
@Mod.EventBusSubscriber(modid = DemoMod.MOD_ID)
public class DemoMod {
	public static final String MOD_ID = "demo";

	/** Much stricter than vanilla (5 days): one in-game day, about 20 minutes. */
	public static final long DEMO_LENGTH_TICKS = 24000L;

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
		if (isDemoOver(server)) {
			for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {
				player.connection.disconnect(Component.literal("The demo is over. Thanks for playing!"));
			}
			return;
		}
		// Reminder every minute.
		if (server.getTickCount() % 1200 == 0) {
			long minutes = (DEMO_LENGTH_TICKS - server.overworld().getGameTime()) / 1200L + 1;
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.displayClientMessage(Component.literal("Demo: " + minutes + " min left. Buy Minecraft: Java Edition to play for real."), true);
			}
		}
	}

	@SubscribeEvent
	public static void onChat(ServerChatEvent event) {
		event.setCanceled(true);
		event.getPlayer().displayClientMessage(Component.literal("Chat is disabled in the demo."), true);
	}

	@SubscribeEvent
	public static void onCommand(CommandEvent event) {
		event.setCanceled(true);
	}
}
