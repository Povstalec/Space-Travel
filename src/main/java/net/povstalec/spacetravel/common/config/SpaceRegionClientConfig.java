package net.povstalec.spacetravel.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.povstalec.spacetravel.SpaceTravel;

public class SpaceRegionClientConfig
{
	public static SpaceTravelConfigValue.IntValue space_region_render_distance;
	
	public static void init(ForgeConfigSpec.Builder client)
	{
		space_region_render_distance = new SpaceTravelConfigValue.IntValue(client,
				"client.space_region_render_distance",
				8, 2, 12,
				"Specifies the max distance at which a Space Region can render");
	}
}
