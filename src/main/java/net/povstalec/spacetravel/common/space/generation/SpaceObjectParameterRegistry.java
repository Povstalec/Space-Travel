package net.povstalec.spacetravel.common.space.generation;

import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.common.space.generation.templates.SpaceObjectParameters;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;

import javax.annotation.Nullable;
import java.util.HashMap;

public class SpaceObjectParameterRegistry
{
	private static final HashMap<ResourceLocation, SpaceObjectParameters> SPACE_OBJECT_TEMPLATES = new HashMap<>();
	
	public static <T extends SpaceObject> void register(ResourceLocation resourceLocation, SpaceObjectParameters template)
	{
		if(SPACE_OBJECT_TEMPLATES.containsKey(resourceLocation))
			throw new IllegalStateException("Duplicate registration for " + resourceLocation.toString());
		
		SPACE_OBJECT_TEMPLATES.put(resourceLocation, template);
	}
	
	@Nullable
	public static SpaceObjectParameters get(ResourceLocation resourceLocation)
	{
		if(!SPACE_OBJECT_TEMPLATES.containsKey(resourceLocation))
			return null;
		
		return SPACE_OBJECT_TEMPLATES.get(resourceLocation);
	}
}
