package net.povstalec.spacetravel.client;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.client.render.level.SpaceShipSpecialEffects;
import net.povstalec.stellarview.client.render.ClientSpaceRegion;
import net.povstalec.stellarview.client.render.SpaceRenderer;
import net.povstalec.stellarview.client.resourcepack.objects.*;

public class ClientAccess
{
	public static final String OBJECT_TYPE = "object_type";
	
	public static final ResourceLocation STAR_FIELD_LOCATION = new ResourceLocation(SpaceTravel.MODID, "star_field");
	public static final ResourceLocation STAR_LOCATION = new ResourceLocation(SpaceTravel.MODID, "star");
	public static final ResourceLocation BLACK_HOLE_LOCATION = new ResourceLocation(SpaceTravel.MODID, "black_hole");
	public static final ResourceLocation ORBITING_OBJECT_LOCATION = new ResourceLocation(SpaceTravel.MODID, "orbiting_object");
	
	protected static Minecraft minecraft = Minecraft.getInstance();
    
    public static void updateDimensions(Set<ResourceKey<Level>> levelKeys, boolean add)
    {
    	final LocalPlayer player = minecraft.player;
        if(player == null)
            return;

        final Set<ResourceKey<Level>> dimensionList = player.connection.levels();
        if(dimensionList == null)
            return;
        
        if(add)
        {
        	for(ResourceKey<Level> levelKey : levelKeys)
            {
            	dimensionList.add(levelKey);
            }
        }
        else
        {
        	for(ResourceKey<Level> levelKey : levelKeys)
            {
            	dimensionList.remove(levelKey);
            }
        }
    }
    
    public static void updateRenderCenter(CompoundTag tag)
    {
		//TODO Is this necessary?
    	/*if(SpaceRenderer.renderCenter == null)
    	{
        	Spaceship ship = new Spaceship();
			TexturedObjectRenderer.Generic testRenderer = new TexturedObjectRenderer.Generic(ship);
        	
    		SpaceRenderer.renderCenter = new RenderCenter();
    		SpaceRenderer.renderCenter.viewCenter = testRenderer;
    	}*/
    }
    
    public static void updateSpaceship(CompoundTag spaceshipTag)
    {
		if(SpaceShipSpecialEffects.spaceshipViewCenter != null)
			SpaceShipSpecialEffects.spaceshipViewCenter.deserializeNBT(spaceshipTag);
    }
    
    public static void clearSpaceRegion()
    {
		SpaceRenderer.clear();
    }
    
    public static void loadSpaceRegion(long x, long y, long z, CompoundTag childrenTag)
    {
    	ClientSpaceRegion spaceRegion = new ClientSpaceRegion(x, y, z);
		
		setupClientSpaceRegion(spaceRegion, childrenTag);
		
		SpaceRenderer.addSpaceRegion(spaceRegion);
    }
    
    public static void unloadSpaceRegion(long x, long y, long z)
    {
    	ClientSpaceRegion.RegionPos spaceRegionPos = new ClientSpaceRegion.RegionPos(x, y, z);
		SpaceRenderer.removeSpaceRegion(spaceRegionPos);
    }
	
	public static void setupClientSpaceRegion(ClientSpaceRegion spaceRegion, CompoundTag childrenTag)
	{
		for(String childId : childrenTag.getAllKeys())
		{
			CompoundTag childTag = childrenTag.getCompound(childId);
			String objectTypeString = childTag.getString(OBJECT_TYPE);
			
			if(objectTypeString != null && ResourceLocation.isValidResourceLocation(objectTypeString))
			{
				SpaceObject spaceObject = null;
				ResourceLocation objectType = new ResourceLocation(objectTypeString);
				// Deserializes object based on its type specified in the object_type
				if(objectType.equals(ORBITING_OBJECT_LOCATION))
					spaceObject = deserializeOrbitingObject(childTag);
				else if(objectType.equals(STAR_LOCATION))
					spaceObject = deserializeStar(childTag);
				else if(objectType.equals(BLACK_HOLE_LOCATION))
					spaceObject = deserializeBlackHole(childTag);
				else if(objectType.equals(STAR_FIELD_LOCATION))
					spaceObject = deserializeStarField(childTag);
				
				if(spaceObject != null)
					spaceRegion.addChild(spaceObject);
			}
		}
	}
	
	private static StarField deserializeStarField(CompoundTag childTag)
	{
		StarField starField = new StarField();
		starField.fromTag(childTag);
		
		return starField;
	}
	
	private static Star deserializeStar(CompoundTag childTag)
	{
		Star star = new Star();
		star.fromTag(childTag);
		
		return star;
	}
	
	private static BlackHole deserializeBlackHole(CompoundTag childTag)
	{
		BlackHole blackHole = new BlackHole();
		blackHole.fromTag(childTag);
		
		return blackHole;
	}
	
	private static OrbitingObject deserializeOrbitingObject(CompoundTag childTag)
	{
		OrbitingObject orbitingObject = new OrbitingObject();
		orbitingObject.fromTag(childTag);
		
		return orbitingObject;
	}
}