package net.povstalec.spacetravel.common.space;

import java.util.ArrayList;
import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;
import net.povstalec.spacetravel.common.util.SpaceCoords;

public final class SpaceRegion implements INBTSerializable<CompoundTag>
{
	public static final String X = "x";
	public static final String Y = "y";
	public static final String Z = "z";

	public static final String CHILDREN = "children";
	
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
		
		//TODO Deserialize children
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
		
		public static final Position fromSpaceCoords(SpaceCoords coords)
		{
			return new Position(coords.x().ly() / 1000, coords.y().ly() / 1000, coords.z().ly() / 1000);
		}
	}
}
