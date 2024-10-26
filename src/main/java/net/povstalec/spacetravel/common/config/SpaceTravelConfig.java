package net.povstalec.spacetravel.common.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.povstalec.spacetravel.SpaceTravel;

import java.io.File;

@Mod.EventBusSubscriber
public class SpaceTravelConfig
{
	private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec COMMON_CONFIG;
	
	private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec CLIENT_CONFIG;
	
	static
	{
		SpaceTravel.LOGGER.info("Setting up Space Travel Config");
		
		COMMON_BUILDER.push("Space Travel Common Config");
		
		COMMON_BUILDER.push("Space Region Config");
		SpaceRegionCommonConfig.init(COMMON_BUILDER);
		COMMON_BUILDER.pop();
		
		COMMON_BUILDER.pop();
		COMMON_CONFIG = COMMON_BUILDER.build();
		
		

		CLIENT_BUILDER.push("Space Travel Client Config");
		
		CLIENT_BUILDER.push("Space Region Config");
		SpaceRegionClientConfig.init(CLIENT_BUILDER);
		CLIENT_BUILDER.pop();
		
		CLIENT_BUILDER.push("Star Field Config");
		StarFieldClientConfig.init(CLIENT_BUILDER);
		CLIENT_BUILDER.pop();
		
		CLIENT_BUILDER.pop();
		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}
	
	public static void loadConfig(ForgeConfigSpec config, String path)
	{
		SpaceTravel.LOGGER.info("Loading Config: " + path);
		final CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();
		SpaceTravel.LOGGER.info("Built config: " + path);
		file.load();
		SpaceTravel.LOGGER.info("Loaded Config: " + path);
		config.setConfig(file);
	}
}
