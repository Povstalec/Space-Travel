package net.povstalec.spacetravel.client.render;

import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.client.RenderCenter;
import net.povstalec.spacetravel.client.space_object.ClientGalaxy;
import net.povstalec.spacetravel.client.space_object.ClientSpaceObject;
import net.povstalec.spacetravel.client.space_object.RenderableSpaceObject;
import net.povstalec.spacetravel.common.space.SpaceRegion.Position;
import net.povstalec.spacetravel.common.space.objects.Galaxy;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;

public final class ClientSpaceRegion
{
	private static final Vector3f NULL_VECTOR = new Vector3f();
	
	private Position pos;
	
	protected ArrayList<RenderableSpaceObject> children = new ArrayList<RenderableSpaceObject>();
	
	public ClientSpaceRegion(Position pos, CompoundTag childrenTag)
	{
		this.pos = pos;
		deserializeChildren(childrenTag);
	}
	
	public ClientSpaceRegion(long x, long y, long z, CompoundTag tag)
	{
		this(new Position(x, y, z), tag);
	}
	
	public ClientSpaceRegion()
	{
		this(0, 0, 0, new CompoundTag());
	}
	
	public Position getRegionPos()
	{
		return pos;
	}
	
	public ArrayList<RenderableSpaceObject> getChildren()
	{
		return children;
	}
	
	public boolean addChild(RenderableSpaceObject child)
	{
		if(this.children.contains(child))
			return false;
		
		this.children.add(child);
		return true;
	}
	
	public void render(RenderCenter viewCenter, RenderableSpaceObject masterParent, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		for(RenderableSpaceObject spaceObject : children)
		{
			if(spaceObject != masterParent) // Makes sure the master parent (usually galaxy) is rendered last, that way stars from other galaxies don't get rendered over planets
				spaceObject.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, NULL_VECTOR);
		}
		
		masterParent.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, NULL_VECTOR);
	}

	private void deserializeChildren(CompoundTag childrenTag)
	{
		SpaceTravel.LOGGER.info("Deserializing region children");
    	for(String childId : childrenTag.getAllKeys())
    	{
    		SpaceTravel.LOGGER.info("Deserializing " + childId);
    		
    		CompoundTag childTag = childrenTag.getCompound(childId);
    		String objectTypeString = childTag.getString(SpaceObject.OBJECT_TYPE);
    		
    		if(objectTypeString != null && ResourceLocation.isValidResourceLocation(objectTypeString))
    		{
        		SpaceTravel.LOGGER.info("Type: " + objectTypeString);
    			RenderableSpaceObject spaceObject = null;
    			ResourceLocation objectType = new ResourceLocation(objectTypeString);
    			
    			// Deserializes object based on its type specified in the object_type
    			if(objectType.equals(SpaceObject.SPACE_OBJECT_LOCATION))
    				spaceObject = deserializeSpaceObject(childTag);
    			else if(objectType.equals(Galaxy.SpiralGalaxy.SPIRAL_GALAXY_LOCATION))
    				spaceObject = deserializeSpiralGalaxy(childTag);
    			
    			//TODO Add event for leftover object types
    			
    			if(spaceObject != null && spaceObject.isInitialized())
    			{

    		    	if(addChild(spaceObject))
    		    		SpaceTravel.LOGGER.info("Added " + childId);
    			}
    		}
    	}
	}
	
	private RenderableSpaceObject deserializeSpaceObject(CompoundTag childTag)
	{
		ClientSpaceObject spaceObject = new ClientSpaceObject();
		spaceObject.deserializeNBT(childTag);
		System.out.println(spaceObject == null);
    	return spaceObject;
	}
	
	private RenderableSpaceObject deserializeSpiralGalaxy(CompoundTag childTag)
	{
		ClientGalaxy.ClientSpiralGalaxy spiralGalaxy = new ClientGalaxy.ClientSpiralGalaxy();
    	spiralGalaxy.deserializeNBT(childTag);
    	
    	return spiralGalaxy;
	}
}
