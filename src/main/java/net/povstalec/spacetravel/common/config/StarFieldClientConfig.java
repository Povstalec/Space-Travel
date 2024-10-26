package net.povstalec.spacetravel.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class StarFieldClientConfig
{
	public static SpaceTravelConfigValue.BooleanValue textured_stars;
	
	public static void init(ForgeConfigSpec.Builder client)
	{
		textured_stars = new SpaceTravelConfigValue.BooleanValue(client,
				"client.textured_stars",
				false,
				"Specifies the max distance at which a Space Region can render");
	}
}
