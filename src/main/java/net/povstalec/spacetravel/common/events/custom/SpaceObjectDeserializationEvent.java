package net.povstalec.spacetravel.common.events.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;

public class SpaceObjectDeserializationEvent extends Event
{
	private final ResourceLocation objectType;
	private final CompoundTag tag;
	private SpaceObject spaceObject;
	
	public SpaceObjectDeserializationEvent(ResourceLocation objectType, CompoundTag tag)
	{
		this.objectType = objectType;
		this.tag = tag;
	}
	
	public ResourceLocation getObjectType()
	{
		return this.objectType;
	}
	
	public CompoundTag getTag()
	{
		return this.tag;
	}
	
	public void setSpaceObject(SpaceObject spaceObject)
	{
		this.spaceObject = spaceObject;
	}
	
	public SpaceObject getSpaceObject()
	{
		return this.spaceObject;
	}
}
