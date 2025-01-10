package net.povstalec.spacetravel.common.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.capabilities.SpaceshipCapability;
import net.povstalec.spacetravel.common.capabilities.SpaceshipCapabilityProvider;
import net.povstalec.spacetravel.common.data.Multiverse;
import net.povstalec.spacetravel.common.init.WorldGenInit;

@Mod.EventBusSubscriber(modid = SpaceTravel.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents
{
	@SubscribeEvent
	public static void onServerStarting(ServerStartingEvent event)
	{
		MinecraftServer server = event.getServer();
		
		Multiverse.get(server).setupUniverse();
	}
	
	@SubscribeEvent
	public static void onTick(TickEvent.LevelTickEvent event)
	{
		Level level = event.level;
		
		if(event.phase.equals(TickEvent.Phase.START) && level != null && !level.isClientSide())
		{
			LazyOptional<SpaceshipCapability> spaceshipCapability = level.getCapability(SpaceshipCapabilityProvider.SPACESHIP);
			
			spaceshipCapability.ifPresent(cap -> 
			{
				if(cap != null)
					cap.spaceship.tick((ServerLevel) level);
			});
		}
	}
	
	@SubscribeEvent
	public static void attachLevelCapabilies(AttachCapabilitiesEvent<Level> event)
	{
		ResourceLocation location = event.getObject().dimensionTypeId().location();
		
		if(location.equals(WorldGenInit.SPACE_TYPE.location()))
		    event.addCapability(new ResourceLocation(SpaceTravel.MODID, "spaceship"), new SpaceshipCapabilityProvider());
	}
	
	@SubscribeEvent
	public static void onRegisterCapabilities(RegisterCapabilitiesEvent event)
	{
		event.register(SpaceshipCapability.class);
	}
	
	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		ServerPlayer player = (ServerPlayer) event.getEntity();
		
		if(player != null && player.level() == null)
			return;
		
		SpaceTravel.updatePlayerRenderer(player.level(), player);
	}
	
	@SubscribeEvent
	public static void onPlayerChangedDimensions(PlayerEvent.PlayerChangedDimensionEvent event)
	{
		ServerPlayer player = (ServerPlayer) event.getEntity();
		
		if(player != null && player.level() == null)
			return;
		
		MinecraftServer server = player.level().getServer();
		
		SpaceTravel.updatePlayerRenderer(server.getLevel(event.getTo()), player);
	}
}
