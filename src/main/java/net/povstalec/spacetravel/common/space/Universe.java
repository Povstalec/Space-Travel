package net.povstalec.spacetravel.common.space;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.joml.Vector3f;

import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.space.objects.Galaxy;
import net.povstalec.spacetravel.common.space.objects.Galaxy.SpiralGalaxy;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;
import net.povstalec.spacetravel.common.util.AxisRotation;
import net.povstalec.spacetravel.common.util.Color;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import net.povstalec.spacetravel.common.util.TextureLayer;
import net.povstalec.spacetravel.common.util.UV;

public class Universe implements INBTSerializable<CompoundTag>
{
	public static final ResourceKey<Registry<Universe>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(SpaceTravel.MODID, "universe"));
	public static final Codec<ResourceKey<Universe>> RESOURCE_KEY_CODEC = ResourceKey.codec(REGISTRY_KEY);
	
	public static final String SPACE_REGIONS = "space_regions";
	
	private final HashMap<SpaceRegion.Position, SpaceRegion> spaceRegions;
	
	public Universe()
	{
		spaceRegions = new HashMap<SpaceRegion.Position, SpaceRegion>();
		
		//TODO Don't keep these here forever
		Galaxy.SpiralGalaxy milkyWay = new Galaxy.SpiralGalaxy(SpiralGalaxy.SPIRAL_GALAXY_LOCATION, Optional.empty(), new SpaceCoords(), new AxisRotation(), new ArrayList<TextureLayer>(), 10842, 90000, 4, 2.5, 1500);
		
		getRegionAt(0, 0, 0).addChild(milkyWay);
    	
    	ArrayList<TextureLayer> texture = new ArrayList<TextureLayer>();
    	texture.add(new TextureLayer(new ResourceLocation("textures/environment/sun.png"), new Color.IntRGBA(255, 255, 255, 255), true, 100, 10, true, 0, new UV.Quad(false)));
    	SpaceObject sun = new SpaceObject(SpiralGalaxy.SPACE_OBJECT_LOCATION, Optional.empty(), new SpaceCoords().add(new Vector3f(1, 0, 0)), new AxisRotation(), texture);

		getRegionAt(0, 0, 0).addChild(sun);
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

		return spaceRegions.computeIfAbsent(pos, position -> SpaceRegion.generateRegion(position, 10428)); // TODO Handle seeds
	}
	
	/**
	 * Method that fetches space regions in a certain radius
	 * @param regionPos
	 * @return Returns a map of space region position + space region of regions in some range around coords
	 */
	public Map<SpaceRegion.Position, SpaceRegion> getRegionsAt(SpaceRegion.Position regionPos, int radius)
	{
		return getRegionsAt(regionPos.x(), regionPos.y(), regionPos.z(), radius);
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
		
		for(long x = -radius + xCenter; x <= radius + xCenter; x++)
		{
			for(long y = -radius + yCenter; y <= radius + yCenter; y++)
			{
				for(long z = -radius + zCenter; z <= radius + zCenter; z++)
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
		CompoundTag spaceRegionsTag =  tag.getCompound(SPACE_REGIONS);
		for(String keyString : spaceRegionsTag.getAllKeys())
		{
			SpaceRegion spaceRegion = new SpaceRegion();
			spaceRegion.deserializeNBT(spaceRegionsTag.getCompound(keyString));
			// Take Space Region's deserialized pos and put it in the map
			spaceRegions.put(spaceRegion.getRegionPos(), spaceRegion);
		}
	}
	
}
