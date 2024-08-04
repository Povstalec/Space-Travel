package net.povstalec.spacetravel.common.space;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.common.events.custom.SpaceObjectDeserializationEvent;
import net.povstalec.spacetravel.common.events.custom.SpaceTravelEvents;
import net.povstalec.spacetravel.common.space.objects.Galaxy;
import net.povstalec.spacetravel.common.space.objects.OrbitingObject;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;

public class SpaceObjectDeserializer
{
	@Nullable
	public static SpaceObject deserialize(ResourceLocation objectType, CompoundTag tag) //TODO Add all the other basic Space Object types //TODO Probably should thrown an exception if a deserialization doesn't work out
	{
		// Deserializes object based on its type specified in the object_type
		if(objectType.equals(OrbitingObject.ORBITING_OBJECT_LOCATION))
			return deserializeOrbitingObject(tag);
		if(objectType.equals(SpaceObject.SPACE_OBJECT_LOCATION))
			return deserializeSpaceObject(tag);
		else if(objectType.equals(Galaxy.SpiralGalaxy.SPIRAL_GALAXY_LOCATION))
			return deserializeSpiralGalaxy(tag);
		
		SpaceObjectDeserializationEvent event = SpaceTravelEvents.onObjectDeserialized(objectType, tag);
		
		if(event.getSpaceObject() != null)
			return event.getSpaceObject();
		
		return null;
	}
	

	@Nullable
	public static SpaceObject deserialize(String objectTypeString, CompoundTag tag) //TODO Add all the other basic Space Object types //TODO Probably should thrown an exception if a deserialization doesn't work out
	{
		System.out.println("Deserialization attempt [" + objectTypeString + "]");
		if(objectTypeString != null && ResourceLocation.isValidResourceLocation(objectTypeString))
			return deserialize(new ResourceLocation(objectTypeString), tag);
		
		return null;
	}
	
	private static SpaceObject deserializeSpaceObject(CompoundTag tag)
	{
		SpaceObject spaceObject = new SpaceObject();
		spaceObject.deserializeNBT(tag);
		System.out.println("Deserialized space object");
		
    	return spaceObject;
	}
	
	private static OrbitingObject deserializeOrbitingObject(CompoundTag tag)
	{
		OrbitingObject orbitingObject = new OrbitingObject();
		orbitingObject.deserializeNBT(tag);
		System.out.println("Deserialized orbiting object");
		
    	return orbitingObject;
	}
	
	private static Galaxy.SpiralGalaxy deserializeSpiralGalaxy(CompoundTag tag)
	{
		Galaxy.SpiralGalaxy spiralGalaxy = new Galaxy.SpiralGalaxy();
    	spiralGalaxy.deserializeNBT(tag);
		System.out.println("Deserialized spiral galaxy");
    	
    	return spiralGalaxy;
	}
}
