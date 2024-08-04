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
        	SpaceObjectRenderer.Generic testRenderer = new SpaceObjectRenderer.Generic(ship);
        	
    		SpaceRenderer.renderCenter = new RenderCenter();
    		SpaceRenderer.renderCenter.viewCenter = testRenderer;
    	}
    	else
    	{
    		if(SpaceRenderer.renderCenter.viewCenter.spaceObject instanceof Spaceship ship)
    		{
    			ship.toggleSpeed();
    		}
    	}
    }
    
    public static void updateSpaceRegion(long x, long y, long z, CompoundTag childrenTag)
    {
    	ClientSpaceRegion spaceRegion = new ClientSpaceRegion(x, y, z, childrenTag);
		SpaceRenderer.addSpaceRegion(spaceRegion);
    	
    	//spaceRegion.addChild(testCenter);
		
    	//SpaceObject testCenter = new SpaceObject(SpaceObject.SPACE_OBJECT_LOCATION, Optional.empty(), new SpaceCoords(), new AxisRotation(), new ArrayList<TextureLayer>());
    	//SpaceObjectRenderer.Generic testRenderer = new SpaceObjectRenderer.Generic(testCenter);
    	
		//SpaceRenderer.renderCenter = new RenderCenter();
		//SpaceRenderer.renderCenter.viewCenter = testRenderer;
    }
}