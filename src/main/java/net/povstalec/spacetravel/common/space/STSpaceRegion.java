package net.povstalec.spacetravel.common.space;

import java.util.Random;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.common.config.SpaceRegionCommonConfig;
import net.povstalec.spacetravel.common.init.SpaceObjectRegistry;
import net.povstalec.stellarview.api.common.SpaceRegion;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.StarField;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;

public final class STSpaceRegion extends SpaceRegion
{
	public static final int SPACE_REGION_LOAD_DISTANCE = SpaceRegionCommonConfig.space_region_load_distance.get();

	public static final String CHILDREN = "children";
	public static final String SAVE = "save";
	
	private boolean shouldSave = false;
	
	public STSpaceRegion(RegionPos pos)
	{
		super(pos);
	}
	
	public STSpaceRegion(long x, long y, long z)
	{
		super(x, y, z);
	}
	
	public STSpaceRegion()
	{
		super(0, 0, 0);
	}
	
	
	
	public void markToSave()
	{
		this.shouldSave = true;
	}
	
	public boolean shouldSave()
	{
		return shouldSave;
	}
	
	
	
	public static STSpaceRegion generateRegion(Universe universe, RegionPos pos, long seed)
	{
		long usedSeed = seed + pos.hashCode();
		
		STSpaceRegion spaceRegion = new STSpaceRegion(pos);
		
		//TODO Random generation
		Random random = new Random(usedSeed);
		
		int chance = random.nextInt(0, 101);
		
		if(chance > 90)
		{
			long randomX = random.nextLong(0, STSpaceRegion.LY_PER_REGION) - STSpaceRegion.LY_PER_REGION_HALF;
			long randomY = random.nextLong(0, STSpaceRegion.LY_PER_REGION) - STSpaceRegion.LY_PER_REGION_HALF;
			long randomZ = random.nextLong(0, STSpaceRegion.LY_PER_REGION) - STSpaceRegion.LY_PER_REGION_HALF;
			
			long x = randomX + pos.lyX();
			long y = randomY + pos.lyY();
			long z = randomZ + pos.lyZ();
			
			double xRot = random.nextDouble(0, 360);
			double yRot = random.nextDouble(0, 360);
			double zRot = random.nextDouble(0, 360);
			
			StarField starField = universe.randomStarFieldTemplate(random).generateStarField(random, usedSeed, new SpaceCoords(x, y, z), new AxisRotation(true, xRot, yRot, zRot));
			spaceRegion.addChild(starField);
		}
		
		return spaceRegion;
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	private static CompoundTag serializeSpaceObject(SpaceObject object)
	{
		CompoundTag objectTag = object.serializeNBT();
		
		ResourceLocation typeLocation = SpaceObjectRegistry.getResourceLocation(object);
		if(typeLocation == null)
			return null;
		
		objectTag.putString(SpaceObjectRegistry.OBJECT_TYPE, typeLocation.toString());
		
		CompoundTag childrenTag = new CompoundTag();
		int i = 0;
		for(SpaceObject child : object.getChildren())
		{
			CompoundTag childTag = serializeSpaceObject(child);
			if(childTag != null)
				childrenTag.put(String.valueOf(i), childTag);
			i++;
		}
		objectTag.put(CHILDREN, childrenTag);
		
		return objectTag;
	}
	
	private static SpaceObject deserializeSpaceObject(CompoundTag tag)
	{
		if(!tag.contains(SpaceObjectRegistry.OBJECT_TYPE))
			return null;
		
		ResourceLocation typeLocation = ResourceLocation.tryParse(tag.getString(SpaceObjectRegistry.OBJECT_TYPE));
		if(typeLocation == null)
			return null;
		
		SpaceObject object = SpaceObjectRegistry.constructObject(typeLocation);
		
		if(object == null)
			return null;
		
		object.deserializeNBT(tag);
		
		if(tag.contains(CHILDREN))
		{
			CompoundTag childrenTag = tag.getCompound(CHILDREN);
			for(int i = 0; i < childrenTag.size(); i++)
			{
				SpaceObject child = deserializeSpaceObject(childrenTag.getCompound(String.valueOf(i)));
				
				if(child != null)
					object.addChildRaw(child);
			}
		}
		
		return object;
	}
	
	public CompoundTag getChildrenTag()
	{
		CompoundTag childrenTag = new CompoundTag();
		int i = 0;
		for(SpaceObject spaceObject : children)
		{
			CompoundTag childTag = serializeSpaceObject(spaceObject);
			
			if(childTag != null)
				childrenTag.put(String.valueOf(i), childTag);
			i++;
		}
		
		return childrenTag;
	}
	
	public void deserializeChildrenTag(CompoundTag childrenTag)
	{
		for(int i = 0; i < childrenTag.size(); i++)
		{
			CompoundTag childTag = childrenTag.getCompound(String.valueOf(i));
			SpaceObject child = deserializeSpaceObject(childTag);
			
			if(child != null)
				addChild(child);
		}
	}
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = super.serializeNBT();
		
		tag.put(CHILDREN, getChildrenTag());
		tag.putBoolean(SAVE, shouldSave);
		
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		shouldSave = tag.getBoolean(SAVE);
		
		CompoundTag childrenTag = tag.getCompound(CHILDREN);
		deserializeChildrenTag(childrenTag);
	}
}
