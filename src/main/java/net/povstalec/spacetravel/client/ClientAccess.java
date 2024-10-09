package net.povstalec.spacetravel.client;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.povstalec.spacetravel.client.render.ClientSpaceRegion;
import net.povstalec.spacetravel.client.render.SpaceRenderer;
import net.povstalec.spacetravel.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.spacetravel.client.render.space_objects.TexturedObjectRenderer;
import net.povstalec.spacetravel.common.space.SpaceRegion;
import net.povstalec.spacetravel.common.space.Spaceship;

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
    	if(SpaceRenderer.renderCenter == null)
    	{
        	Spaceship ship = new Spaceship();
			TexturedObjectRenderer.Generic testRenderer = new TexturedObjectRenderer.Generic(ship);
        	
    		SpaceRenderer.renderCenter = new RenderCenter();
    		SpaceRenderer.renderCenter.viewCenter = testRenderer;
    	}
    }
    
    public static void updateSpaceship(CompoundTag spaceshipTag)
    {
    	if(SpaceRenderer.renderCenter != null && SpaceRenderer.renderCenter.viewCenter != null &&
    			SpaceRenderer.renderCenter.viewCenter.spaceObject instanceof Spaceship spaceship)
    	{
    		spaceship.deserializeNBT(spaceshipTag);
    	}
    }
    
    public static void clearSpaceRegion()
    {
		SpaceRenderer.clear();
    }
    
    public static void loadSpaceRegion(long x, long y, long z, CompoundTag childrenTag)
    {
    	ClientSpaceRegion spaceRegion = new ClientSpaceRegion(x, y, z, childrenTag);
		SpaceRenderer.addSpaceRegion(spaceRegion);
    }
    
    public static void unloadSpaceRegion(long x, long y, long z)
    {
    	SpaceRegion.Position spaceRegionPos = new SpaceRegion.Position(x, y, z);
		SpaceRenderer.removeSpaceRegion(spaceRegionPos);
    }
}