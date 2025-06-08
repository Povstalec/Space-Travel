package net.povstalec.spacetravel.common.space;

import net.minecraft.server.MinecraftServer;

/**
 * Interface that allows loading additional info for a Space Object while the region is being loaded
 */
public interface LoadableObject
{
	boolean isLoaded();
	
	void load(MinecraftServer server);
}
