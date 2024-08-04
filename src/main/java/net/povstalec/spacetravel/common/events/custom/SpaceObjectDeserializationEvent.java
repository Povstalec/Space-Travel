package net.povstalec.spacetravel.common.events.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;

public class SpaceObjectDeserializationEvent extends Event
{
	private final ResourceLocation objectType;
	private final CompoundTag spaceObjectTag;
	private SpaceObject spaceObject;
	
	/**
	 * Fires when a Space Object with an unknown type is being deserialized
	 * You can subscribe to this event and use custom deserialize your custom objects yourself and then set them
	 * @param objectType
	 * @param tag
	 * @return
	 */
	public SpaceObjectDeserializationEvent(ResourceLocation objectType, CompoundTag spaceObjectTag)
	{
		this.objectType = objectType;
		this.spaceObjectTag = spaceObjectTag;
	}
	
	public ResourceLocation getObjectType()
	{
		return this.objectType;
	}
	
	public CompoundTag getSpaceObjectTag()
	{
		return this.spaceObjectTag;
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
