package net.povstalec.spacetravel.common.space;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.config.SpaceRegionCommonConfig;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;
import net.povstalec.spacetravel.common.space.objects.StarField;
import net.povstalec.spacetravel.common.util.AxisRotation;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import net.povstalec.spacetravel.common.util.StarInfo;
import net.povstalec.spacetravel.common.util.TextureLayer;

public final class SpaceRegion implements INBTSerializable<CompoundTag>
{
	public static final int SPACE_REGION_LOAD_DISTANCE = SpaceRegionCommonConfig.space_region_load_distance.get();
	
	public static final String X = "x";
	public static final String Y = "y";
	public static final String Z = "z";

	public static final String CHILDREN = "children";
	
	public static final String SAVE = "save";
	
	public static final long LY_PER_REGION = 2000000;
	public static final long LY_PER_REGION_HALF = LY_PER_REGION / 2;
	
	private Position pos;
	
	protected ArrayList<SpaceObject> children = new ArrayList<SpaceObject>();
	
	private boolean shouldSave = false;
	
	public SpaceRegion(Position pos)
	{
		this.pos = pos;
	}
	
	public SpaceRegion(long x, long y, long z)
	{
		this(new Position(x, y, z));
	}
	
	public SpaceRegion()
	{
		this(0, 0, 0);
	}
	
	public Position getRegionPos()
	{
		return pos;
	}
	
	public ArrayList<SpaceObject> getChildren()
	{
		return children;
	}
	
	public void addChild(SpaceObject child)
	{
		if(!this.children.contains(child))
		{
			this.children.add(child);
			
			markToSave();
		}
		else
			SpaceTravel.LOGGER.error("Region " + this.toString() + " already contains " + child.toString());
		//TODO Handle this somewhere higher, ideally do something similar to the Stargate Network, which needs to be reloaded
	}
	
	public void markToSave()
	{
		this.shouldSave = true;
	}
	
	public boolean shouldSave()
	{
		return shouldSave;
	}
	
	public static SpaceRegion generateRegion(Position pos, long seed)
	{
		long usedSeed = seed + pos.hashCode();
		
		SpaceRegion spaceRegion = new SpaceRegion(pos);
		
		//TODO Random generation
		Random random = new Random(usedSeed);
		
		int chance = random.nextInt(0, 101);
		
		if(chance >= 80)
		{
			StarField starField = StarField.randomStarField(random, usedSeed, pos.lyX(), pos.lyY(), pos.lyZ());
			spaceRegion.addChild(starField);
		}
		
		return spaceRegion;
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();

		tag.putLong(X, pos.x());
		tag.putLong(Y, pos.y());
		tag.putLong(Z, pos.z());
		
		tag.put(CHILDREN, getChildrenTag());
		
		tag.putBoolean(SAVE, shouldSave);
		
		return tag;
	}
	
	public CompoundTag getChildrenTag()
	{
		CompoundTag childrenTag = new CompoundTag();
		int i = 0;
		for(SpaceObject spaceObject : children)
		{
			childrenTag.put(String.valueOf(i), spaceObject.serializeNBT());
			i++;
		}
		
		return childrenTag;
	}

	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		pos = new Position(tag.getLong(X), tag.getLong(Y), tag.getLong(Z));
		
		shouldSave = tag.getBoolean(SAVE);
		
		CompoundTag childrenTag = tag.getCompound(CHILDREN);
		for(int i = 0; i < childrenTag.size(); i++)
		{
			CompoundTag childTag = childrenTag.getCompound(String.valueOf(i));
			
			SpaceObject spaceObject = SpaceObjectDeserializer.deserialize(childTag.getString(SpaceObject.OBJECT_TYPE), childTag);
			
			if(spaceObject != null && spaceObject.isInitialized())
				addChild(spaceObject);
		}
	}
	
	/**
	 * Immutable Position object used for dividing up objects in space, so that they're not all loaded
	 */
	public static final class Position
	{
		private long x, y, z;
		
		public Position(long x, long y, long z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public Position(SpaceCoords coords)
		{
			this(	(coords.x().ly() - LY_PER_REGION_HALF) / LY_PER_REGION,
					(coords.y().ly() - LY_PER_REGION_HALF) / LY_PER_REGION,
					(coords.z().ly() - LY_PER_REGION_HALF) / LY_PER_REGION);
		}
		
		public long x()
		{
			return x;
		}
		
		public long y()
		{
			return y;
		}
		
		public long z()
		{
			return z;
		}
		
		public long lyX()
		{
			return x * SpaceRegion.LY_PER_REGION;
		}
		
		public long lyY()
		{
			return y * SpaceRegion.LY_PER_REGION;
		}
		
		public long lyZ()
		{
			return z * SpaceRegion.LY_PER_REGION;
		}
		
		public boolean isInRange(Position other, int range)
		{
			return Math.abs(this.x - other.x) < range && Math.abs(this.y - other.y) < range && Math.abs(this.z - other.z) < range;
		}
		
		@Override
		public final boolean equals(Object object)
		{
			if(object instanceof Position pos)
				return this.x == pos.x && this.y == pos.y && this.z == pos.z;
			
			return false;
		}
		
		@Override
		public final int hashCode()
		{
			int result = (int) x;
			result = 31 * result + (int) y;
			result = 31 * result + (int) z;
			return result;
		}
		
		@Override
		public String toString()
		{
			return "{x: " + x + ", y: " + y + ", z: " + z + "}";
		}
	}
}
