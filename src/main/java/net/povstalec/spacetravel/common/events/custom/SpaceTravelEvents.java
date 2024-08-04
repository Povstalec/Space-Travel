package net.povstalec.spacetravel.common.events.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class SpaceTravelEvents
{
	public static SpaceObjectDeserializationEvent onObjectDeserialized(ResourceLocation objectType, CompoundTag tag)
    {
		SpaceObjectDeserializationEvent event = new SpaceObjectDeserializationEvent(objectType, tag);
        MinecraftForge.EVENT_BUS.post(event);
        
        return event;
    }
}
