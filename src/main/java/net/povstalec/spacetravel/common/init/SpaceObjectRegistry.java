package net.povstalec.spacetravel.common.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.*;

import javax.annotation.Nullable;
import java.util.HashMap;

public class SpaceObjectRegistry
{
	public static final String OBJECT_TYPE = "object_type";
	
	public static final ResourceLocation PLANET_LOCATION = new ResourceLocation(SpaceTravel.MODID, "celestials/planet");
	public static final ResourceLocation MOON_LOCATION = new ResourceLocation(SpaceTravel.MODID, "celestials/moon");
	public static final ResourceLocation STAR_LOCATION = new ResourceLocation(SpaceTravel.MODID, "celestials/star");
	public static final ResourceLocation BLACK_HOLE_LOCATION = new ResourceLocation(SpaceTravel.MODID, "celestials/black_hole");
	public static final ResourceLocation NEBULA_LOCATION = new ResourceLocation(SpaceTravel.MODID, "celestials/nebula");
	public static final ResourceLocation STAR_FIELD_LOCATION = new ResourceLocation(SpaceTravel.MODID, "celestials/star_field");
	
	public static final ResourceKey<Registry<Planet>> PLANET_REGISTRY_KEY = ResourceKey.createRegistryKey(PLANET_LOCATION);
	public static final ResourceKey<Registry<Moon>> MOON_REGISTRY_KEY = ResourceKey.createRegistryKey(MOON_LOCATION);
	public static final ResourceKey<Registry<Star>> STAR_REGISTRY_KEY = ResourceKey.createRegistryKey(STAR_LOCATION);
	public static final ResourceKey<Registry<BlackHole>> BLACK_HOLE_REGISTRY_KEY = ResourceKey.createRegistryKey(BLACK_HOLE_LOCATION);
	public static final ResourceKey<Registry<Nebula>> NEBULA_REGISTRY_KEY = ResourceKey.createRegistryKey(NEBULA_LOCATION);
	public static final ResourceKey<Registry<StarField>> STAR_FIELD_REGISTRY_KEY = ResourceKey.createRegistryKey(STAR_FIELD_LOCATION);
	
	private static final HashMap<ResourceLocation, SpaceObjectConstructor> SPACE_OBJECTS = new HashMap<ResourceLocation, SpaceObjectConstructor>();
	private static final HashMap<Class<? extends SpaceObject>, ResourceLocation> LOCATIONS = new HashMap<Class<? extends SpaceObject>, ResourceLocation>();
	
	public static <T extends SpaceObject> void register(ResourceLocation resourceLocation, Class<T> objectClass, SpaceObjectConstructor<T> constructor)
	{
		if(SPACE_OBJECTS.containsKey(resourceLocation))
			throw new IllegalStateException("Duplicate registration for " + resourceLocation.toString());
		if(LOCATIONS.containsKey(objectClass))
			throw new IllegalStateException("Duplicate registration for " + objectClass.getName());
		
		SPACE_OBJECTS.put(resourceLocation, constructor);
		LOCATIONS.put(objectClass, resourceLocation);
	}
	
	@Nullable
	public static SpaceObject constructObject(ResourceLocation resourceLocation)
	{
		if(SPACE_OBJECTS.containsKey(resourceLocation))
			return SPACE_OBJECTS.get(resourceLocation).create();
		
		return null;
	}
	
	@Nullable
	public static ResourceLocation getResourceLocation(SpaceObject object)
	{
		if(object != null && LOCATIONS.containsKey(object.getClass()))
			return LOCATIONS.get(object.getClass());
		
		return null;
	}
	
	
	
	public interface SpaceObjectConstructor<T extends SpaceObject>
	{
		T create();
	}
}
