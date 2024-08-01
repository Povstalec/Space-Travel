package net.povstalec.spacetravel.common.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.data.Space;

@Mod.EventBusSubscriber(modid = SpaceTravel.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents
{
	@SubscribeEvent
	public static void onServerStarting(ServerStartingEvent event)
	{
		SpaceTravel.LOGGER.info("--------------------HELLO from server starting--------------------");
		MinecraftServer server = event.getServer();
		
		Space.get(server).setupUniverse();
	}
	
	@SubscribeEvent
	public static void attachWorldCapabilies(AttachCapabilitiesEvent<Level> event)
	{
		ResourceLocation location = event.getObject().dimensionTypeId().location();
		
		//if(location.equals(WorldGenInit.SPACE_TYPE.location()))
		//    event.addCapability(new ResourceLocation(AstralVoyage.MODID, "view_center"), new GenericProvider<>(CapabilitiesInit.VIEW_CENTER, new ViewCenterCapability()));
	}
}
