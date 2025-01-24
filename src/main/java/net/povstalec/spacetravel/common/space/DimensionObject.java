package net.povstalec.spacetravel.common.space;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface DimensionObject
{
	public static final String DIMENSION = "dimension";
	
	ResourceKey<Level> dimension();
}
