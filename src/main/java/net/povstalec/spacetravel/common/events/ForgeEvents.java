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
import net.povstalec.spacetravel.common.capabilities.ViewObjectCapability;
import net.povstalec.spacetravel.common.capabilities.ViewObjectCapabilityProvider;
import net.povstalec.spacetravel.common.data.Multiverse;
import net.povstalec.spacetravel.common.init.WorldGenInit;
import net.povstalec.spacetravel.common.space.Spaceship;
import net.povstalec.spacetravel.common.space.Universe;

import java.util.Optional;

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
			level.getCapability(SpaceshipCapabilityProvider.SPACESHIP).ifPresent(cap -> cap.spaceship.tick((ServerLevel) level));
	}
	
	@SubscribeEvent
	public static void attachLevelCapabilies(AttachCapabilitiesEvent<Level> event)
	{
		ResourceLocation location = event.getObject().dimensionTypeId().location();
		
		if(location.equals(WorldGenInit.SPACE_TYPE.location()))
		{
			Spaceship spaceship = new Spaceship();
			event.addCapability(new ResourceLocation(SpaceTravel.MODID, "spaceship"), new SpaceshipCapabilityProvider(spaceship));
			event.addCapability(new ResourceLocation(SpaceTravel.MODID, "view_object"), new ViewObjectCapabilityProvider(spaceship));
		}
		else if(location.equals(WorldGenInit.PLANET_TYPE.location()))
			event.addCapability(new ResourceLocation(SpaceTravel.MODID, "view_object"), new ViewObjectCapabilityProvider(null));
	}
	
	@SubscribeEvent
	public static void onRegisterCapabilities(RegisterCapabilitiesEvent event)
	{
		event.register(SpaceshipCapability.class);
		event.register(ViewObjectCapability.class);
	}
	
	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		ServerPlayer player = (ServerPlayer) event.getEntity();
		
		if(player == null)
			return;
		
		Optional<Universe> universe = Multiverse.get(player.level()).getUniverse(Multiverse.PRIME_UNIVERSE);
		universe.ifPresent(value -> player.level().getCapability(ViewObjectCapabilityProvider.VIEW_OBJECT).ifPresent(cap ->
		{
			if(cap.viewObject() == null)
				cap.loadRegion(player.getServer(), value);
		}));
		
		SpaceTravel.updatePlayerRenderer(player.level(), player);
	}
	
	@SubscribeEvent
	public static void onPlayerChangedDimensions(PlayerEvent.PlayerChangedDimensionEvent event)
	{
		ServerPlayer player = (ServerPlayer) event.getEntity();
		
		if(player == null)
			return;
		
		MinecraftServer server = player.level().getServer();
		Optional<Universe> universe = Multiverse.get(player.level()).getUniverse(Multiverse.PRIME_UNIVERSE);
		universe.ifPresent(value -> player.level().getCapability(ViewObjectCapabilityProvider.VIEW_OBJECT).ifPresent(cap ->
		{
			if(cap.viewObject() == null)
				cap.loadRegion(player.getServer(), value);
		}));
		
		ServerLevel newLevel = server.getLevel(event.getTo());
		if(newLevel != null)
			SpaceTravel.updatePlayerRenderer(newLevel, player);
	}
}
