package net.povstalec.spacetravel.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.client.RenderCenter;
import net.povstalec.spacetravel.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.spacetravel.client.render.space_objects.StarFieldRenderer;
import net.povstalec.spacetravel.common.space.SpaceRegion.Position;
import net.povstalec.spacetravel.common.space.objects.OrbitingObject;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;
import net.povstalec.spacetravel.common.space.objects.StarField;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

public final class ClientSpaceRegion
{
	private static final Vector3f NULL_VECTOR = new Vector3f();
	
	private Position pos;
	
	protected ArrayList<SpaceObjectRenderer<?>> children = new ArrayList<SpaceObjectRenderer<?>>();
	
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
	
	public ArrayList<SpaceObjectRenderer<?>> getChildren()
	{
		return children;
	}
	
	public boolean addChild(SpaceObjectRenderer<?> child)
	{
		if(this.children.contains(child))
			return false;
		
		this.children.add(child);
		return true;
	}
	
	public void render(RenderCenter viewCenter, SpaceObjectRenderer<?> masterParent, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		for(SpaceObjectRenderer<?> spaceObject : children)
		{
			if(spaceObject != masterParent) // Makes sure the master parent (usually galaxy) is rendered last, that way stars from other galaxies don't get rendered over planets
				spaceObject.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, NULL_VECTOR);
		}
		
		masterParent.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, NULL_VECTOR);
	}

	private void deserializeChildren(CompoundTag childrenTag)
	{
		for(String childId : childrenTag.getAllKeys())
		{
			
			CompoundTag childTag = childrenTag.getCompound(childId);
			String objectTypeString = childTag.getString(SpaceObject.OBJECT_TYPE);
			
			if(objectTypeString != null && ResourceLocation.isValidResourceLocation(objectTypeString))
			{
				SpaceObjectRenderer<?> spaceObjectRenderer = null;
				ResourceLocation objectType = new ResourceLocation(objectTypeString);
				
				// Deserializes object based on its type specified in the object_type
				if(objectType.equals(OrbitingObject.ORBITING_OBJECT_LOCATION))
					spaceObjectRenderer = deserializeOrbitingObject(childTag);
				if(objectType.equals(SpaceObject.SPACE_OBJECT_LOCATION))
					spaceObjectRenderer = deserializeSpaceObject(childTag);
				else if(objectType.equals(StarField.STAR_FIELD_LOCATION))
					spaceObjectRenderer = deserializeSpiralGalaxy(childTag);
				
				//TODO Add event for leftover object types
				
				if(spaceObjectRenderer != null)
					addChild(spaceObjectRenderer);
			}
		}
	}
	
	private SpaceObjectRenderer<SpaceObject> deserializeSpaceObject(CompoundTag childTag)
	{
		SpaceObject spaceObject = new SpaceObject();
		spaceObject.deserializeNBT(childTag);
    	
    	if(spaceObject.isInitialized())
    		return new SpaceObjectRenderer<SpaceObject>(spaceObject);
		
    	return null;
	}
	
	private SpaceObjectRenderer<OrbitingObject> deserializeOrbitingObject(CompoundTag childTag)
	{
		OrbitingObject orbitingObject = new OrbitingObject();
		orbitingObject.deserializeNBT(childTag);
    	
    	if(orbitingObject.isInitialized())
    		return new SpaceObjectRenderer<OrbitingObject>(orbitingObject);
		
    	return null;
	}
	
	private StarFieldRenderer<StarField> deserializeSpiralGalaxy(CompoundTag childTag)
	{
		StarField starField = new StarField();
		starField.deserializeNBT(childTag);
		
		System.out.println("Deserialized Seed: " + starField.getSeed()); //TODO Remove
    	
    	if(starField.isInitialized())
    		return new StarFieldRenderer<StarField>(starField);
		
    	return null;
	}
}
