package net.povstalec.spacetravel.common.space;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.spacetravel.SpaceTravel;

public class Universe implements INBTSerializable<CompoundTag>
{
	public static final ResourceKey<Registry<Universe>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(SpaceTravel.MODID, "universe"));
	public static final Codec<ResourceKey<Universe>> RESOURCE_KEY_CODEC = ResourceKey.codec(REGISTRY_KEY);
	
	public static final String SPACE_REGIONS = "space_regions";
	
	private final HashMap<SpaceRegion.Position, SpaceRegion> spaceRegions;
	
	public Universe()
	{
		spaceRegions = new HashMap<SpaceRegion.Position, SpaceRegion>();
	}
	
	/**
	 * Method for getting a space region at specific coordinates
	 * @param x
	 * @param y
	 * @param z
	 * @return Returns an existing space region located at xyz coordinates or a new space region if it doesn't exist yet
	 */
	public SpaceRegion getRegionAt(long x, long y, long z)
	{
		SpaceRegion.Position pos = new SpaceRegion.Position(x, y, z);
		
		return spaceRegions.computeIfAbsent(pos, position -> new SpaceRegion(position));
	}
	
	/**
	 * Method that fetches space regions in a certain radius
	 * @param x
	 * @param y
	 * @param z
	 * @return Returns a map of space region position + space region of regions in some range around coords
	 */
	public Map<SpaceRegion.Position, SpaceRegion> getRegionsAt(long xCenter, long yCenter, long zCenter, int radius)
	{
		HashMap<SpaceRegion.Position, SpaceRegion> spaceRegions = new HashMap<SpaceRegion.Position, SpaceRegion>();
		
		for(long x = -radius + xCenter; x <= radius; x++)
		{
			for(long y = -radius + yCenter; y <= radius; y++)
			{
				for(long z = -radius + zCenter; z <= radius; z++)
				{
					SpaceRegion spaceRegion = getRegionAt(x, y, z);
					spaceRegions.put(spaceRegion.getRegionPos(), spaceRegion);
				}
			}
		}
		
		return spaceRegions;
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
			spaceRegionsTag.put(spaceRegionEntry.getKey().toString(), spaceRegionEntry.getValue().serializeNBT());
		}
		
		tag.put(SPACE_REGIONS, spaceRegionsTag);
		
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		for(String keyString : tag.getAllKeys())
		{
			SpaceRegion spaceRegion = new SpaceRegion();
			spaceRegion.deserializeNBT(tag.getCompound(keyString));
			// Take Space Region's deserialized pos and put it in the map
			spaceRegions.put(spaceRegion.getRegionPos(), spaceRegion);
		}
	}
	
}
