package net.povstalec.spacetravel.common.space;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.function.Predicate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.povstalec.spacetravel.common.config.SpaceRegionCommonConfig;
import net.povstalec.spacetravel.common.init.SpaceObjectInit;
import net.povstalec.spacetravel.common.space.generation.parameters.SpaceObjectParameters;
import net.povstalec.stellarview.api.common.SpaceRegion;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import org.jetbrains.annotations.Nullable;

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
	
	
	
	public static SpaceCoords randomRegionCoords(Random random, RegionPos pos)
	{
		long randomX = random.nextLong(0, STSpaceRegion.LY_PER_REGION) - STSpaceRegion.LY_PER_REGION_HALF;
		long randomY = random.nextLong(0, STSpaceRegion.LY_PER_REGION) - STSpaceRegion.LY_PER_REGION_HALF;
		long randomZ = random.nextLong(0, STSpaceRegion.LY_PER_REGION) - STSpaceRegion.LY_PER_REGION_HALF;
		
		return new SpaceCoords(randomX + pos.lyX(), randomY + pos.lyY(), randomZ + pos.lyZ());
	}
	
	public static AxisRotation randomAxisRotation(Random random)
	{
		double xRot = random.nextDouble(0, 360);
		double yRot = random.nextDouble(0, 360);
		double zRot = random.nextDouble(0, 360);
		
		return new AxisRotation(xRot, yRot, zRot);
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
			SpaceObjectParameters parameters = universe.randomSpaceObjectParameters(random);
			if(parameters != null)
			{
				SpaceObject spaceObject = parameters.generate(random, usedSeed, randomRegionCoords(random, pos), randomAxisRotation(random));
				if(spaceObject != null)
					spaceRegion.addChild(spaceObject);
			}
		}
		
		return spaceRegion;
	}
	
	
	
	private void loadObjectRecursive(MinecraftServer server, SpaceObject spaceObject)
	{
		if(spaceObject instanceof LoadableObject loadableObject)
		{
			loadableObject.load(server);
		}
		
		for(SpaceObject child : spaceObject.getChildren())
		{
			loadObjectRecursive(server, child);
		}
	}
	
	/**
	 * Loads all loadable objects in the region
	 * @param server
	 */
	public void load(MinecraftServer server)
	{
		for(SpaceObject child : getChildren())
		{
			loadObjectRecursive(server, child);
		}
	}
	
	@Nullable
	public SpaceObject findClosest(SpaceCoords coords, Predicate<SpaceObject> filter)
	{
		SpaceObject closest = null;
		double closestDistSqr = -1;
		
		Queue<SpaceObject> queue = new LinkedList<>();
		queue.addAll(getChildren());
		
		while(!queue.isEmpty())
		{
			SpaceObject front = queue.remove();
			queue.addAll(front.getChildren());
			
			if(filter.test(front))
			{
				double distSqr = front.getCoords().distanceSquared(coords);
				if((closestDistSqr == -1 || distSqr < closestDistSqr))
				{
					closest = front;
					closestDistSqr = distSqr;
				}
			}
		}
		
		return closest;
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	private static CompoundTag serializeSpaceObject(SpaceObject object)
	{
		CompoundTag objectTag = object.serializeNBT();
		
		ResourceLocation typeLocation = SpaceObjectInit.getResourceLocation(object);
		if(typeLocation == null)
			return null;
		
		objectTag.putString(SpaceObjectInit.OBJECT_TYPE, typeLocation.toString());
		
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
		if(!tag.contains(SpaceObjectInit.OBJECT_TYPE))
			return null;
		
		ResourceLocation typeLocation = ResourceLocation.tryParse(tag.getString(SpaceObjectInit.OBJECT_TYPE));
		if(typeLocation == null)
			return null;
		
		SpaceObject object = SpaceObjectInit.constructObject(typeLocation);
		
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
