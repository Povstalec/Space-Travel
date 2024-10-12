package net.povstalec.spacetravel.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.povstalec.spacetravel.SpaceTravel;

public class SpaceRegionCommonConfig
{
	public static SpaceTravelConfigValue.IntValue space_region_load_distance;
	
	public static void init(ForgeConfigSpec.Builder client)
	{
		space_region_load_distance = new SpaceTravelConfigValue.IntValue(client,
				"server.space_region_load_distance",
				3, 2, 10,
				"Specifies the max distance at which a Space Region can load on the Server");
	}
}
