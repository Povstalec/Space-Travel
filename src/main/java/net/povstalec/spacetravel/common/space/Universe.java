package net.povstalec.spacetravel.common.space;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;
import net.povstalec.spacetravel.common.space.objects.StarField;
import net.povstalec.spacetravel.common.util.*;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Universe implements INBTSerializable<CompoundTag>
{
	public static final ResourceKey<Registry<Universe>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(SpaceTravel.MODID, "universe"));
	public static final Codec<ResourceKey<Universe>> RESOURCE_KEY_CODEC = ResourceKey.codec(REGISTRY_KEY);
	
	public static final String SPACE_REGIONS = "space_regions";
	
	private final HashMap<SpaceRegion.Position, SpaceRegion> spaceRegions;
	
	public Universe()
	{
		spaceRegions = new HashMap<SpaceRegion.Position, SpaceRegion>();
		
		StarField.SpiralArm arm1 = new StarField.SpiralArm(1500, 0, 1.6298770314100501, 2.0, true);
		StarField.SpiralArm arm2 = new StarField.SpiralArm(1500, 90, 1.6941251689586823, 2.0, true);
		StarField.SpiralArm arm3 = new StarField.SpiralArm(1500, 180, 1.9660513834462778, 2.0, true);
		StarField.SpiralArm arm4 = new StarField.SpiralArm(1500, 270, 1.8086568638593041, 2.0, true);
		
		ArrayList<StarField.SpiralArm> arms = new ArrayList<StarField.SpiralArm>();
		arms.add(arm1);
		arms.add(arm2);
		arms.add(arm3);
		arms.add(arm4);
		
		//TODO Don't keep these here forever
		StarField milkyWay = new StarField(StarField.STAR_FIELD_LOCATION, Optional.empty(), new SpaceCoords(0L, 0L, 28000L),
				new AxisRotation(0, 0, 0), new ArrayList<TextureLayer>(),
				StarInfo.DEFAULT_STAR_INFO, 10842L, 90000, 1500, true, 0.5, 0.25, 0.5, arms);
		
		StarField.SpiralArm arm5 = new StarField.SpiralArm(4000, 0, 2.92, 2.5, true);
		StarField.SpiralArm arm6 = new StarField.SpiralArm(4000, 180, 2.56, 2.5, true);
		
		ArrayList<StarField.SpiralArm> armsA = new ArrayList<StarField.SpiralArm>();
		armsA.add(arm5);
		armsA.add(arm6);
		
		//TODO Don't keep these here forever
		StarField andromeda = new StarField(StarField.STAR_FIELD_LOCATION, Optional.empty(), new StellarCoordinates.Equatorial(
				new StellarCoordinates.RightAscension(0, 42, 44.3), new StellarCoordinates.Declination(41, 16, 9),
				new SpaceCoords.SpaceDistance(2500000)),
				new AxisRotation(0, 32, -49), new ArrayList<TextureLayer>(),
				StarInfo.DEFAULT_STAR_INFO, 55183L, 152000, 4000, true, 0.5, 0.25, 0.5, armsA);
		
		addToRegion(milkyWay);
		addToRegion(andromeda);
    	
    	ArrayList<TextureLayer> texture = new ArrayList<TextureLayer>();
    	texture.add(new TextureLayer(new ResourceLocation("textures/environment/sun.png"), new Color.IntRGBA(255, 255, 255, 255), true, 100, 0.4, true, 0, new UV.Quad(false)));
    	SpaceObject sun = new SpaceObject(StarField.STAR_FIELD_LOCATION, Optional.empty(), Either.left(new SpaceCoords()), new AxisRotation(), texture);

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
		
		return getRegionAt(pos);
	}
	
	/**
	 * Method for getting a space region at specific coordinates
	 * @param pos Position of the Space Region
	 * @return Returns an existing space region located at xyz coordinates or a new space region if it doesn't exist yet
	 */
	public SpaceRegion getRegionAt(SpaceRegion.Position pos)
	{
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
	 * @param xCenter
	 * @param yCenter
	 * @param zCenter
	 * @param radius
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
	
	/**
	 * Method for adding an object to the Space Region
	 * @param spaceObject Space Object that should be added
	 * @return Returns true if
	 */
	public void addToRegion(SpaceObject spaceObject)
	{
		SpaceRegion.Position pos = new SpaceRegion.Position(spaceObject.getSpaceCoords());
		
		getRegionAt(pos).addChild(spaceObject);
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
