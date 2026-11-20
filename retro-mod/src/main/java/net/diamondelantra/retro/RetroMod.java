package net.diamondelantra.retro;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Old-version feel, server side: no attack cooldown (the 1.9 combat system did not exist yet).
 * Sprinting is removed on the client in RetroClient.
 */
public class RetroMod implements ModInitializer {
	@Override
	public void onInitialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20 != 0) return;
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				EntityAttributeInstance speed = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
				if (speed != null && speed.getBaseValue() < 100.0) speed.setBaseValue(100.0);
			}
		});
	}
}
