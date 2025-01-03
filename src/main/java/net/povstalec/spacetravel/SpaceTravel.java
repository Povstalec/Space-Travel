package net.povstalec.spacetravel;

import java.util.Map;
import java.util.Optional;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.povstalec.spacetravel.common.config.SpaceTravelConfig;
import net.povstalec.spacetravel.common.space.generation.StarFieldTemplate;
import net.povstalec.spacetravel.common.space.objects.BlackHole;
import net.povstalec.spacetravel.common.space.objects.Star;
import net.povstalec.spacetravel.common.space.objects.StarField;
import net.povstalec.stellarview.client.events.StellarViewReloadEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.povstalec.spacetravel.client.render.level.SpaceTravelDimensionSpecialEffects;
import net.povstalec.spacetravel.common.capabilities.SpaceshipCapability;
import net.povstalec.spacetravel.common.capabilities.SpaceshipCapabilityProvider;
import net.povstalec.spacetravel.common.data.Multiverse;
import net.povstalec.spacetravel.common.init.CommandInit;
import net.povstalec.spacetravel.common.init.PacketHandlerInit;
import net.povstalec.spacetravel.common.init.WorldGenInit;
import net.povstalec.spacetravel.common.packets.ClientBoundRenderCenterUpdatePacket;
import net.povstalec.spacetravel.common.packets.ClientBoundSpaceRegionClearPacket;
import net.povstalec.spacetravel.common.packets.ClientBoundSpaceRegionLoadPacket;
import net.povstalec.spacetravel.common.space.SpaceRegion;
import net.povstalec.spacetravel.common.space.Spaceship;
import net.povstalec.spacetravel.common.space.Universe;

@Mod(SpaceTravel.MODID)
public class SpaceTravel
{
	public static final String MODID = "spacetravel";
	
	public static final Logger LOGGER = LogUtils.getLogger();
	
	public SpaceTravel()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		modEventBus.addListener((DataPackRegistryEvent.NewRegistry event) ->
		{
			event.dataPackRegistry(Universe.REGISTRY_KEY, Universe.CODEC, Universe.CODEC);
			
			event.dataPackRegistry(StarFieldTemplate.REGISTRY_KEY, StarFieldTemplate.CODEC, StarFieldTemplate.CODEC);
			
			event.dataPackRegistry(StarField.REGISTRY_KEY, StarField.CODEC, StarField.CODEC);
			event.dataPackRegistry(Star.REGISTRY_KEY, Star.CODEC, Star.CODEC);
			event.dataPackRegistry(BlackHole.REGISTRY_KEY, BlackHole.CODEC, BlackHole.CODEC);
		});
		
		modEventBus.addListener(this::commonSetup);
		
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SpaceTravelConfig.CLIENT_CONFIG, MODID + "-client.toml");
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SpaceTravelConfig.COMMON_CONFIG, MODID + "-common.toml");
		
        WorldGenInit.register(modEventBus);
		
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.addListener(CommandInit::registerCommands);
	}
	
	private void commonSetup(final FMLCommonSetupEvent event)
	{
		event.enqueueWork(() -> 
    	{
            PacketHandlerInit.register();
    	});
	}
	
	// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents
	{
		@SubscribeEvent
		public static void registerDimensionalEffects(RegisterDimensionSpecialEffectsEvent event)
		{
			SpaceTravelDimensionSpecialEffects.register(event);
		}
	}
	
	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
	public static class ClientForgeEvents
	{
		@SubscribeEvent
		public static void stellarViewReload(StellarViewReloadEvent event)
		{
			//event.setCanceled(true);
		}
	}
	
	public static void updatePlayerRenderer(Level level, ServerPlayer player)
	{
		LazyOptional<SpaceshipCapability> spaceshipCapability = level.getCapability(SpaceshipCapabilityProvider.SPACESHIP);
		
		spaceshipCapability.ifPresent(cap -> 
		{
			if(cap != null)
			{
				Optional<Universe> universe = Multiverse.get(level).getUniverse(Multiverse.PRIME_UNIVERSE); //TODO There can be other universes
				
				if(universe.isPresent())
				{
					PacketHandlerInit.sendToPlayer(player, new ClientBoundRenderCenterUpdatePacket(new Spaceship())); //TODO Get coords from somewhere
					PacketHandlerInit.sendToPlayer(player, new ClientBoundSpaceRegionClearPacket());
					for(Map.Entry<SpaceRegion.Position, SpaceRegion> spaceRegionEntry : universe.get().getRegionsAt(new SpaceRegion.Position(cap.spaceship.getSpaceCoords()), SpaceRegion.SPACE_REGION_LOAD_DISTANCE, true).entrySet())
					{
						PacketHandlerInit.sendToPlayer(player, new ClientBoundSpaceRegionLoadPacket(spaceRegionEntry.getValue()));
					}
				}
			}
		});
	}
}
