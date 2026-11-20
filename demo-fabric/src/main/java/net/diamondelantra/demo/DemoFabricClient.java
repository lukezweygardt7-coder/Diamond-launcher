package net.diamondelantra.demo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.Text;

/** Client half of the Fabric demo: servers are not part of the demo, only the built-in singleplayer server. */
public class DemoFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (!client.isIntegratedServerRunning()) {
				handler.getConnection().disconnect(Text.literal("Multiplayer is not available in demo mode."));
			}
		});
	}
}
