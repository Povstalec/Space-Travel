package net.povstalec.spacetravel.common.data;

import java.util.*;

import javax.annotation.Nonnull;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.init.SpaceObjectRegistry;
import net.povstalec.spacetravel.common.space.Universe;
import net.povstalec.spacetravel.common.space.generation.SpaceObjectParameterRegistry;
import net.povstalec.spacetravel.common.space.generation.templates.StarFieldParameters;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.BlackHole;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Nebula;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Star;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.StarField;

public class Multiverse extends SavedData
{
	private static final String FILE_NAME = SpaceTravel.MODID + "-multiverse";

	private static final String UNIVERSES = "universes";
	
	public static final ResourceLocation PRIME_UNIVERSE = new ResourceLocation(SpaceTravel.MODID, "universe/prime");

	private HashMap<ResourceLocation, Universe> universes = new HashMap<ResourceLocation, Universe>();
	
	private MinecraftServer server;
	
	/*public final void updateData(MinecraftServer server)
	{
		eraseData(server); //TODO Does this really need any erasing?
		
		registerSpaceObjectFromDataPacks(server);
	}

	public void eraseData(MinecraftServer server)
	{
		this.spaceObjects.clear();
		this.setDirty();
	}*/
	
	public void setupUniverse()
	{
		//TODO Universe setup event
		
		registerUniverses(server);
		
		registerStarFieldParameters(server);
		
		registerStarFields(server);
		registerStars(server);
		registerBlackHoles(server);
		registerNebulae(server);
		
		prepareUniverses();
		
		this.setDirty();
	}
	
	public void registerUniverses(MinecraftServer server)
	{
		final RegistryAccess registries = server.registryAccess();
		final Registry<Universe> starRegistry = registries.registryOrThrow(Universe.REGISTRY_KEY);
		
		Set<Map.Entry<ResourceKey<Universe>, Universe>> universeSet = starRegistry.entrySet();
		universeSet.forEach((universeEntry) ->
		{
			Universe universe = universeEntry.getValue();
			ResourceLocation location = universeEntry.getKey().location().withPath("universe/" + universeEntry.getKey().location().getPath());
			
			universe.setResourceLocation(location);
			universe.setupSeed(server.getWorldData().worldGenOptions().seed());
			
			universes.put(location, universe);
		});
		SpaceTravel.LOGGER.info("Universes registered");
	}
	
	public Optional<Universe> getUniverse(ResourceLocation location)
	{
		if(!universes.containsKey(location))
			return Optional.empty();
		
		return Optional.of(universes.get(location));
	}
	
	private void prepareUniverses()
	{
		for(Map.Entry<ResourceLocation, Universe> universeEntry : universes.entrySet())
		{
			universeEntry.getValue().prepareObjects();
		}
	}
	
	public void registerStarFieldParameters(MinecraftServer server)
	{
		final RegistryAccess registries = server.registryAccess();
		final Registry<StarFieldParameters> templateRegistry = registries.registryOrThrow(StarFieldParameters.REGISTRY_KEY);
		
		Set<Map.Entry<ResourceKey<StarFieldParameters>, StarFieldParameters>> templateSet = templateRegistry.entrySet();
		templateSet.forEach((templateEntry) ->
		{
			ResourceLocation location = templateEntry.getKey().location();
			StarFieldParameters template = templateEntry.getValue();
			
			SpaceObjectParameterRegistry.register(new ResourceLocation(location.getNamespace(), "star_field/"+ location.getPath()), template);
		});
		SpaceTravel.LOGGER.info("Star Field Templates registered");
	}

	//============================================================================================
	//*********************************Registering Space Objects**********************************
	//============================================================================================
	
	private void addObjectToAllUniverses(ResourceLocation location, SpaceObject spaceObject)
	{
		for(Map.Entry<ResourceLocation, Universe> universeEntry : universes.entrySet())
		{
			universeEntry.getValue().addSpaceObject(location, spaceObject);
		}
	}
	
	public void registerStarFields(MinecraftServer server)
	{
		final RegistryAccess registries = server.registryAccess();
		final Registry<StarField> starFieldRegistry = registries.registryOrThrow(SpaceObjectRegistry.STAR_FIELD_REGISTRY_KEY);
		
		Set<Map.Entry<ResourceKey<StarField>, StarField>> starFieldSet = starFieldRegistry.entrySet();
		starFieldSet.forEach((starFieldEntry) ->
		{
			StarField starField = starFieldEntry.getValue();
			ResourceLocation location = starFieldEntry.getKey().location().withPath("star_field/" + starFieldEntry.getKey().location().getPath());
			
			starField.setResourceLocation(location);
			
			addObjectToAllUniverses(location, starField);
		});
		SpaceTravel.LOGGER.info("Star Fields registered");
	}
	
	public void registerStars(MinecraftServer server)
	{
		final RegistryAccess registries = server.registryAccess();
		final Registry<Star> starRegistry = registries.registryOrThrow(SpaceObjectRegistry.STAR_REGISTRY_KEY);
		
		Set<Map.Entry<ResourceKey<Star>, Star>> starSet = starRegistry.entrySet();
		starSet.forEach((starEntry) ->
		{
			Star star = starEntry.getValue();
			ResourceLocation location = starEntry.getKey().location().withPath("star/" + starEntry.getKey().location().getPath());
			
			star.setResourceLocation(location);
			
			addObjectToAllUniverses(location, star);
		});
		SpaceTravel.LOGGER.info("Stars registered");
	}
	
	public void registerBlackHoles(MinecraftServer server)
	{
		final RegistryAccess registries = server.registryAccess();
		final Registry<BlackHole> starRegistry = registries.registryOrThrow(SpaceObjectRegistry.BLACK_HOLE_REGISTRY_KEY);
		
		Set<Map.Entry<ResourceKey<BlackHole>, BlackHole>> blackHoleSet = starRegistry.entrySet();
		blackHoleSet.forEach((blackHoleEntry) ->
		{
			BlackHole blackHole = blackHoleEntry.getValue();
			ResourceLocation location = blackHoleEntry.getKey().location().withPath("black_hole/" + blackHoleEntry.getKey().location().getPath());
			
			blackHole.setResourceLocation(location);
			
			addObjectToAllUniverses(location, blackHole);
		});
		SpaceTravel.LOGGER.info("Black Holes registered");
	}
	
	public void registerNebulae(MinecraftServer server)
	{
		final RegistryAccess registries = server.registryAccess();
		final Registry<Nebula> starRegistry = registries.registryOrThrow(SpaceObjectRegistry.NEBULA_REGISTRY_KEY);
		
		Set<Map.Entry<ResourceKey<Nebula>, Nebula>> nebulaSet = starRegistry.entrySet();
		nebulaSet.forEach((nebulaEntry) ->
		{
			Nebula nebula = nebulaEntry.getValue();
			ResourceLocation location = nebulaEntry.getKey().location().withPath("nebula/" + nebulaEntry.getKey().location().getPath());
			
			nebula.setResourceLocation(location);
			
			addObjectToAllUniverses(location, nebula);
		});
		SpaceTravel.LOGGER.info("Black Holes registered");
	}

	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================

	private CompoundTag serialize()
	{
		CompoundTag tag = new CompoundTag();
		
		tag.put(UNIVERSES, serializeUniverses());
		
		return tag;
	}

	private CompoundTag serializeUniverses()
	{
		CompoundTag tag = new CompoundTag();
		
		for(Map.Entry<ResourceLocation, Universe> universeEntry : universes.entrySet())
		{
			tag.put(universeEntry.getKey().toString(), universeEntry.getValue().serializeNBT());
		}
		
		return tag;
	}
	
	private void deserialize(CompoundTag tag)
	{
		deserializeUniverses(tag.getCompound(UNIVERSES));
	}

	private void deserializeUniverses(CompoundTag tag)
	{
		SpaceTravel.LOGGER.info("Deserializing Universes");
		
		for(String name : tag.getAllKeys())
		{
			Universe universe = new Universe();
			universe.deserializeNBT(tag.getCompound(name));
			
			universes.put(new ResourceLocation(name), universe);
			
			SpaceTravel.LOGGER.info("Deserialized " + name);
		}

	}
	
	//============================================================================================
	//********************************************Data********************************************
	//============================================================================================

	public Multiverse(MinecraftServer server)
	{
		this.server = server;
	}
	
	public static Multiverse create(MinecraftServer server)
	{
		return new Multiverse(server);
	}
	
	public static Multiverse load(MinecraftServer server, CompoundTag tag)
	{
		Multiverse data = create(server);

		data.server = server;
		data.deserialize(tag);
		
		return data;
	}
	
	public CompoundTag save(CompoundTag tag)
	{
		tag = serialize();
		
		return tag;
	}
	
	@Nonnull
	public static Multiverse get(Level level)
	{
		if(level.isClientSide())
			throw new RuntimeException("Don't access this client-side!");
		
		return Multiverse.get(level.getServer());
	}
	
	@Nonnull
	public static Multiverse get(MinecraftServer server)
	{
		DimensionDataStorage storage = server.overworld().getDataStorage();
		
		return storage.computeIfAbsent((tag) -> load(server, tag), () -> create(server), FILE_NAME);
	}
}
