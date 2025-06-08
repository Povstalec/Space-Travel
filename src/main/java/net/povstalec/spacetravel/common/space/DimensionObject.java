package net.povstalec.spacetravel.common.space;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.povstalec.spacetravel.common.space.generation.parameters.WorldGenInfo;
import net.povstalec.spacetravel.common.util.DimensionUtil;

import javax.annotation.Nullable;

public interface DimensionObject
{
	String DIMENSION = "dimension";
	
	@Nullable
	ResourceKey<Level> dimension();
	
	@Nullable
	WorldGenInfo worldGenInfo();
	
	boolean hasSurface();
	
	@Nullable
	ServerLevel generateWorld(MinecraftServer server);
	
	@Nullable
	ServerLevel getLevel(MinecraftServer server, boolean generate);
}
