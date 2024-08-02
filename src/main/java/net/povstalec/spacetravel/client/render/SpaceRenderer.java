package net.povstalec.spacetravel.client.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.spacetravel.client.ViewCenter;
import net.povstalec.spacetravel.client.space_object.RenderableSpaceObject;

public class SpaceRenderer
{
	private static final List<RenderableSpaceObject> SPACE_OBJECTS = new ArrayList<RenderableSpaceObject>();
	private static final Vector3f NULL_VECTOR = new Vector3f();
	
	public static ViewCenter viewCenter;
	
	
	
	public static void clear()
	{
		SPACE_OBJECTS.clear();
	}
	
	public static void addSpaceObject(RenderableSpaceObject spaceObject)
	{
		SPACE_OBJECTS.add(spaceObject);
	}
	
	public static void removeSpaceObject(RenderableSpaceObject spaceObject)
	{
		SPACE_OBJECTS.remove(spaceObject);
	}
	
	public static void render(ViewCenter viewCenter, RenderableSpaceObject masterParent, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		for(RenderableSpaceObject spaceObject : SPACE_OBJECTS)
		{
			if(spaceObject != masterParent) // Makes sure the master parent (usually galaxy) is rendered last, that way stars from other galaxies don't get rendered over planets
				spaceObject.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, NULL_VECTOR);
		}
		
		masterParent.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, NULL_VECTOR);
	}
}
