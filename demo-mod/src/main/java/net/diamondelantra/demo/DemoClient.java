package net.diamondelantra.demo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

/** Client half of the demo: the title screen opens the demo world, and multiplayer is off. */
@Mod.EventBusSubscriber(modid = DemoMod.MOD_ID, value = Dist.CLIENT)
public class DemoClient {

	private static String keyOf(AbstractWidget widget) {
		if (widget.getMessage().getContents() instanceof TranslatableContents contents) {
			return contents.getKey();
		}
		return "";
	}

	@SubscribeEvent
	public static void onTitleScreen(ScreenEvent.Init.Post event) {
		if (!(event.getScreen() instanceof TitleScreen screen)) return;

		for (var listener : new ArrayList<>(event.getListenersList())) {
			if (!(listener instanceof AbstractWidget widget)) continue;
			String key = keyOf(widget);

			if (key.equals("menu.singleplayer")) {
				Button demo = Button.builder(Component.literal("Play Demo World"), b -> openDemoWorld(screen))
					.bounds(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight())
					.build();
				event.removeListener(widget);
				event.addListener(demo);
			} else if (key.equals("menu.multiplayer") || key.equals("menu.online")) {
				widget.active = false;
			}
		}
	}

	private static void openDemoWorld(TitleScreen screen) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.getLevelSource().levelExists("Demo_World")) {
			mc.createWorldOpenFlows().loadLevel(screen, "Demo_World");
		} else {
			mc.createWorldOpenFlows().createFreshLevel("Demo_World", MinecraftServer.DEMO_SETTINGS,
				WorldOptions.DEMO_OPTIONS, WorldPresets::createNormalWorldDimensions);
		}
	}

	/** Servers are not part of the demo. Only the built-in (memory) connection is allowed. */
	@SubscribeEvent
	public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
		if (!event.getConnection().isMemoryConnection()) {
			event.getConnection().disconnect(Component.literal("Multiplayer is not available in demo mode."));
		}
	}
}
