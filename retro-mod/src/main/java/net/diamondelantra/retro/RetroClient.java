package net.diamondelantra.retro;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.TitleScreen;
import org.lwjgl.glfw.GLFW;

/** Client side: old window title, old title-screen label, and no sprinting. */
public class RetroClient implements ClientModInitializer {
	private int ticks;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null && client.player.isSprinting()) client.player.setSprinting(false);
			if (++ticks % 100 == 1 && client.getWindow() != null) {
				GLFW.glfwSetWindowTitle(client.getWindow().getHandle(), Retro.TITLE);
			}
		});

		ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
			if (screen instanceof TitleScreen) {
				ScreenEvents.afterRender(screen).register((s, context, mouseX, mouseY, delta) ->
						context.drawTextWithShadow(client.textRenderer, Retro.TITLE, 2, 2, 0xFFFFFF));
			}
		});
	}
}
