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
import net.povstalec.spacetravel.common.init.SpaceObjectRegistry;
import net.povstalec.spacetravel.common.space.STSpaceRegion;
import net.povstalec.stellarview.api.common.SpaceRegion;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.client.SpaceObjectRenderers;
import net.povstalec.stellarview.client.render.SpaceRegionRenderer;
import net.povstalec.stellarview.client.render.SpaceRenderer;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;

public class ClientAccess
{
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
    	SpaceRegionRenderer spaceRegion = new SpaceRegionRenderer(new SpaceRegion(x, y, z));
		
		setupClientSpaceRegion(spaceRegion, childrenTag);
		
		SpaceRenderer.addSpaceRegion(spaceRegion);
    }
    
    public static void unloadSpaceRegion(long x, long y, long z)
    {
    	SpaceRegion.RegionPos spaceRegionPos = new SpaceRegion.RegionPos(x, y, z);
		SpaceRenderer.removeSpaceRegion(spaceRegionPos);
    }
	
	private static void deserializeObjectsRecursive(SpaceObjectRenderer parent, CompoundTag tag)
	{
		String objectTypeString = tag.getString(SpaceObjectRegistry.OBJECT_TYPE);
		
		if(objectTypeString != null && ResourceLocation.isValidResourceLocation(objectTypeString))
		{
			ResourceLocation objectType = new ResourceLocation(objectTypeString);
			SpaceObject spaceObject = deserialize(objectType, tag);
			
			if(spaceObject != null)
			{
				SpaceObjectRenderer renderer = SpaceObjectRenderers.constructObjectRenderer(spaceObject);
				if(renderer != null)
				{
					parent.addChild(renderer);
					
					if(tag.contains(STSpaceRegion.CHILDREN))
					{
						CompoundTag childrenTag = tag.getCompound(STSpaceRegion.CHILDREN);
						for(String childId : childrenTag.getAllKeys())
						{
							deserializeObjectsRecursive(renderer, childrenTag.getCompound(childId));
						}
					}
				}
			}
		}
	}
	
	public static void setupClientSpaceRegion(SpaceRegionRenderer spaceRegion, CompoundTag tag)
	{
		for(String regionChildId : tag.getAllKeys())
		{
			CompoundTag childTag = tag.getCompound(regionChildId);
			String objectTypeString = childTag.getString(SpaceObjectRegistry.OBJECT_TYPE);
			
			if(objectTypeString != null && ResourceLocation.isValidResourceLocation(objectTypeString))
			{
				ResourceLocation objectType = new ResourceLocation(objectTypeString);
				SpaceObject spaceObject = deserialize(objectType, childTag);
				if(spaceObject != null)
				{
					SpaceObjectRenderer renderer = SpaceObjectRenderers.constructObjectRenderer(spaceObject);
					if(renderer != null)
					{
						spaceRegion.addChild(renderer);
						
						if(tag.contains(STSpaceRegion.CHILDREN))
						{
							CompoundTag childrenTag = tag.getCompound(STSpaceRegion.CHILDREN);
							for(String childId : childrenTag.getAllKeys())
							{
								deserializeObjectsRecursive(renderer, childrenTag.getCompound(childId));
							}
						}
					}
				}
			}
		}
	}
	
	private static SpaceObject deserialize(ResourceLocation typeLocation, CompoundTag tag)
	{
		SpaceObject spaceObject = SpaceObjectRegistry.constructObject(typeLocation);
		if(spaceObject != null)
		{
			spaceObject.deserializeNBT(tag);
			
			return spaceObject;
		}
		
		return null;
	}
}