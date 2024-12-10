package net.povstalec.spacetravel.common.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.space.generation.StarFieldTemplate;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;

import javax.annotation.Nullable;
import java.util.*;

public class Universe implements INBTSerializable<CompoundTag>
{
	public static final ResourceKey<Registry<Universe>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(SpaceTravel.MODID, "universe"));
	public static final Codec<ResourceKey<Universe>> RESOURCE_KEY_CODEC = ResourceKey.codec(REGISTRY_KEY);
	
	public static final String SPACE_REGIONS = "space_regions";
	public static final String SEED = "seed";
	
	//TODO Change it to list
	private final ArrayList<StarFieldTemplate> starFieldTemplates = new ArrayList<StarFieldTemplate>();
	private int totalStarFieldTemplateWeight = 0;
	
	private final HashMap<SpaceRegion.Position, SpaceRegion> spaceRegions;
	private final HashMap<ResourceLocation, SpaceObject> spaceObjects; // Map of space objects that need to be added to this Universe
	
	@Nullable
	private ResourceLocation location;
	@Nullable
	private Long seed;
	
	public static final Codec<Universe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.optionalFieldOf(SEED).forGetter(universe -> Optional.ofNullable(universe.seed)),
			Area.CODEC.listOf().optionalFieldOf("saved_regions", new ArrayList<Area>()).forGetter(universe -> new ArrayList<Area>())
	).apply(instance, Universe::new));
	
	public Universe()
	{
		spaceRegions = new HashMap<SpaceRegion.Position, SpaceRegion>();
		spaceObjects = new HashMap<ResourceLocation, SpaceObject>();
	}
	
	public Universe(Optional<Long> seed, List<Area> forceSavedRegions)
	{
		this();
		
		if(seed.isPresent())
			this.seed = seed.get();
		
		for(Area area : forceSavedRegions)
		{
			for(long x = area.x; x <= area.xEnd; x++)
			{
				for(long y = area.y; y <= area.yEnd; y++)
				{
					for(long z = area.z; z <= area.zEnd; z++)
					{
						getRegionAt(x, y, z, false).markToSave();
					}
				}
			}
		}
	}
	
	public void setupSeed(long seed)
	{
		if(this.seed == null)
			this.seed = seed;
	}
	
	public long getSeed()
	{
		if(seed == null)
			return 0;
		
		return seed;
	}
	
	public void setResourceLocation(ResourceLocation location)
	{
		this.location = location;
	}
	
	@Nullable
	public ResourceLocation getLocation()
	{
		return location;
	}
	
	public void addStarFieldTemplate(StarFieldTemplate template)
	{
		starFieldTemplates.add(template);
		totalStarFieldTemplateWeight += template.getWeight();
	}
	
	public StarFieldTemplate randomStarFieldTemplate(Random random)
	{
		if(starFieldTemplates.isEmpty())
			return StarFieldTemplate.DEFAULT_STAR_FIELD_TEMPLATE;
		
		int i = 0;
		
		for(int weight = random.nextInt(0, totalStarFieldTemplateWeight); i < starFieldTemplates.size() - 1; i++)
		{
			weight -= starFieldTemplates.get(i).getWeight();
			
			if(weight <= 0)
				break;
		}
		
		return starFieldTemplates.get(i);
	}
	
	/**
	 * Method for getting a space region at specific coordinates
	 * @param x
	 * @param y
	 * @param z
	 * @return Returns an existing space region located at xyz coordinates or a new space region if it doesn't exist yet
	 */
	public SpaceRegion getRegionAt(long x, long y, long z, boolean generate)
	{
		SpaceRegion.Position pos = new SpaceRegion.Position(x, y, z);
		
		return getRegionAt(pos, generate);
	}
	
	/**
	 * Method for getting a space region at specific coordinates
	 * @param pos Position of the Space Region
	 * @return Returns an existing space region located at xyz coordinates or a new space region if it doesn't exist yet
	 */
	public SpaceRegion getRegionAt(SpaceRegion.Position pos, boolean generate)
	{
		if(!generate)
			return spaceRegions.computeIfAbsent(pos, position -> new SpaceRegion(pos));
		else
			return spaceRegions.computeIfAbsent(pos, position -> SpaceRegion.generateRegion(this, position, getSeed())); // TODO Handle seeds
	}
	
	/**
	 * Method that fetches space regions in a certain radius
	 * @param regionPos Position of the center
	 * @param radius Radius around which to pick regions
	 * @param generate Whether or not new regions should attempt generating space objects
	 * @return Returns a map of space region position + space region of regions in some range around coords
	 */
	public Map<SpaceRegion.Position, SpaceRegion> getRegionsAt(SpaceRegion.Position regionPos, int radius, boolean generate)
	{
		return getRegionsAt(regionPos.x(), regionPos.y(), regionPos.z(), radius, generate);
	}
	
	/**
	 * Method that fetches space regions in a certain radius
	 * @param xCenter X-position of the center
	 * @param yCenter Y-position of the center
	 * @param zCenter Z-position of the center
	 * @param radius Radius around which to pick regions
	 * @param generate Whether or not new regions should attempt generating space objects
	 * @return Returns a map of space region position + space region of regions in some range around coords
	 */
	public Map<SpaceRegion.Position, SpaceRegion> getRegionsAt(long xCenter, long yCenter, long zCenter, int radius, boolean generate)
	{
		HashMap<SpaceRegion.Position, SpaceRegion> spaceRegions = new HashMap<SpaceRegion.Position, SpaceRegion>();
		
		for(long x = -radius + xCenter; x <= radius + xCenter; x++)
		{
			for(long y = -radius + yCenter; y <= radius + yCenter; y++)
			{
				for(long z = -radius + zCenter; z <= radius + zCenter; z++)
				{
					SpaceRegion spaceRegion = getRegionAt(x, y, z, generate);
					spaceRegions.put(spaceRegion.getRegionPos(), spaceRegion);
				}
			}
		}
		
		return spaceRegions;
	}
	
	/**
	 * Method for adding an object to the Space Region
	 * @param spaceObject Space Object that should be added
	 */
	public void addToRegion(SpaceObject spaceObject)
	{
		SpaceRegion.Position pos = new SpaceRegion.Position(spaceObject.getSpaceCoords());
		
		getRegionAt(pos, false).addChild(spaceObject);
	}
	
	public void addSpaceObject(ResourceLocation location, SpaceObject spaceObject)
	{
		if(!spaceObjects.containsKey(location))
		{
			spaceObjects.put(location, spaceObject);
			SpaceTravel.LOGGER.debug("Added " + spaceObject.toString() + " to Universe " + this.toString());
		}
		else
			SpaceTravel.LOGGER.error("Universe " + this.toString() + " already contains " + spaceObject.toString());
	}
	
	public void prepareObjects()
	{
		for(Map.Entry<ResourceLocation, SpaceObject> spaceObjectEntry : spaceObjects.entrySet())
		{
			SpaceObject spaceObject = spaceObjectEntry.getValue();
			
			// Set name
			spaceObject.setResourceLocation(spaceObjectEntry.getKey());
			
			// Handle parents
			if(spaceObject.getParentLocation().isPresent())
			{
				for(Map.Entry<ResourceLocation, SpaceObject> parentEntry : spaceObjects.entrySet())
				{
					if(parentEntry.getKey().equals(spaceObject.getParentLocation().get()))
					{
						parentEntry.getValue().addChild(spaceObject);
						break;
					}
				}
				
				if(!spaceObject.getParent().isPresent())
					SpaceTravel.LOGGER.error("Failed to find parent for " + spaceObject.toString());
			}
			else
				addToRegion(spaceObjectEntry.getValue());
		}
		spaceObjects.clear();
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();
		
		CompoundTag spaceRegionsTag = new CompoundTag();
		
		for(Map.Entry<SpaceRegion.Position, SpaceRegion> spaceRegionEntry : spaceRegions.entrySet())
		{
			if(spaceRegionEntry.getValue().shouldSave())
				spaceRegionsTag.put(spaceRegionEntry.getKey().toString(), spaceRegionEntry.getValue().serializeNBT());
		}
		
		tag.put(SPACE_REGIONS, spaceRegionsTag);
		tag.putLong(SEED, getSeed());
		
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		CompoundTag spaceRegionsTag =  tag.getCompound(SPACE_REGIONS);
		for(String keyString : spaceRegionsTag.getAllKeys())
		{
			SpaceRegion spaceRegion = new SpaceRegion();
			spaceRegion.deserializeNBT(spaceRegionsTag.getCompound(keyString));
			// Take Space Region's deserialized pos and put it in the map
			spaceRegions.put(spaceRegion.getRegionPos(), spaceRegion);
		}
		
		seed = tag.getLong(SEED);
	}
	
	
	
	public static class Area
	{
		public long x, y, z;
		public long xEnd, yEnd, zEnd;
		
		public static final Codec<Area> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.LONG.fieldOf("x").forGetter(area -> area.x),
				Codec.LONG.fieldOf("y").forGetter(area -> area.y),
				Codec.LONG.fieldOf("z").forGetter(area -> area.z),
				
				Codec.LONG.optionalFieldOf("x_end").forGetter(area -> Optional.ofNullable(area.xEnd)),
				Codec.LONG.optionalFieldOf("y_end").forGetter(area -> Optional.ofNullable(area.yEnd)),
				Codec.LONG.optionalFieldOf("z_end").forGetter(area -> Optional.ofNullable(area.zEnd))
		).apply(instance, Area::new));
		
		public Area(long x, long y, long z, Optional<Long> xEnd, Optional<Long> yEnd, Optional<Long> zEnd)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			
			if(xEnd.isPresent())
				this.xEnd = xEnd.get();
			else
				this.xEnd = x;
			
			if(yEnd.isPresent())
				this.yEnd = yEnd.get();
			else
				this.yEnd = y;
			
			if(zEnd.isPresent())
				this.zEnd = zEnd.get();
			else
				this.zEnd = z;
		}
	}
}
