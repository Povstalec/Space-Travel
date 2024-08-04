package net.povstalec.spacetravel.common.space;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.spacetravel.common.space.objects.Galaxy;
import net.povstalec.spacetravel.common.space.objects.Galaxy.SpiralGalaxy;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;
import net.povstalec.spacetravel.common.util.AxisRotation;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import net.povstalec.spacetravel.common.util.TextureLayer;

public final class SpaceRegion implements INBTSerializable<CompoundTag>
{
	public static final String X = "x";
	public static final String Y = "y";
	public static final String Z = "z";

	public static final String CHILDREN = "children";
	
	public static final long LY_PER_REGION = 1000000;
	
	private Position pos;
	
	protected ArrayList<SpaceObject> children = new ArrayList<SpaceObject>();
	
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
		this.children.add(child);
	}
	
	public static SpaceRegion generateRegion(Position pos, long seed)
	{
		long usedSeed = seed + pos.hashCode();
		
		SpaceRegion spaceRegion = new SpaceRegion(pos);
		
		//TODO Random generation
		Random random = new Random(usedSeed);
		
		int chance = random.nextInt(0, 100);
		
		if(chance > 25)
		{
			long x = random.nextLong(0, LY_PER_REGION) + pos.x() * LY_PER_REGION;
			long y = random.nextLong(0, LY_PER_REGION) + pos.y() * LY_PER_REGION;
			long z = random.nextLong(0, LY_PER_REGION) + pos.z() * LY_PER_REGION;
			
			double xRot = random.nextDouble(0, 360);
			double yRot = random.nextDouble(0, 360);
			double zRot = random.nextDouble(0, 360);
			
			int stars = random.nextInt(400, 3000); // 1500
			int diameter = stars * 60; // 90000
			short numberOfArms = (short) random.nextInt(2, 4); // 4
			double spread = random.nextDouble(2, 5); // 2.5
			
			//seed = 10842
			Galaxy.SpiralGalaxy spiralGalaxy = new Galaxy.SpiralGalaxy(SpiralGalaxy.SPIRAL_GALAXY_LOCATION, Optional.empty(), new SpaceCoords(x, y, z), new AxisRotation(xRot, yRot, zRot), new ArrayList<TextureLayer>(), usedSeed, diameter, numberOfArms, spread, stars);
			
			spaceRegion.addChild(spiralGalaxy);
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
		private long x;
		private long y;
		private long z;
		
		public Position(long x, long y, long z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public Position(SpaceCoords coords)
		{
			this((long) Math.floor((double) coords.x().ly() / LY_PER_REGION), (long) Math.floor((double) coords.y().ly() / LY_PER_REGION), (long) Math.floor((double) coords.z().ly() / LY_PER_REGION));
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
			return Objects.hash(x, y, z);
		}
		
		@Override
		public String toString()
		{
			return "{x: " + x + ", y: " + y + ", z: " + z + "}";
		}
	}
}
