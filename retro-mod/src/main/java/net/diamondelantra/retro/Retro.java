package net.diamondelantra.retro;

import net.fabricmc.loader.api.FabricLoader;

/** Which retro flavour is installed. alpha.jar and beta.jar are the same code built with a different mod id. */
public final class Retro {
	private Retro() {}

	public static final boolean ALPHA = FabricLoader.getInstance().isModLoaded("retro_alpha");

	/** Text shown by the game in place of the modern version name. */
	public static final String TITLE = ALPHA ? "Minecraft Alpha v1.2.6" : "Minecraft Beta 1.7.3";
}
