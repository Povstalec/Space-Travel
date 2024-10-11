package net.povstalec.spacetravel.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.povstalec.spacetravel.SpaceTravel;

public class SpaceRegionClientConfig
{
	public static SpaceTravelConfigValue.IntValue space_region_render_distance; //TODO Use this
	
	public static void init(ForgeConfigSpec.Builder client)
	{
		space_region_render_distance = new SpaceTravelConfigValue.IntValue(client,
				"server.space_region_render_distance",
				3, 1, 10,
				"Specifies the max distance at which a Space Region can render");
	}
}
