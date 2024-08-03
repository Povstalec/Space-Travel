package net.povstalec.spacetravel.client.render;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.spacetravel.client.RenderCenter;
import net.povstalec.spacetravel.client.space_object.RenderableSpaceObject;
import net.povstalec.spacetravel.common.space.SpaceRegion;

public class SpaceRenderer
{
	private static final HashMap<SpaceRegion.Position, ClientSpaceRegion> SPACE_REGIONS = new HashMap<SpaceRegion.Position, ClientSpaceRegion>();
	
	public static RenderCenter viewCenter;
	
	
	
	public static void clear()
	{
		SPACE_REGIONS.clear();
	}
	
	public static void addSpaceRegion(ClientSpaceRegion spaceRegion)
	{
		SPACE_REGIONS.put(spaceRegion.getRegionPos(), spaceRegion);
	}
	
	public static void removeSpaceRegion(SpaceRegion.Position regionPos)
	{
		SPACE_REGIONS.remove(regionPos);
	}
	
	public static void render(RenderCenter viewCenter, RenderableSpaceObject masterParent, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		for(Map.Entry<SpaceRegion.Position, ClientSpaceRegion> spaceRegionEntry : SPACE_REGIONS.entrySet())
		{
			spaceRegionEntry.getValue().render(viewCenter, masterParent, level, camera, partialTicks, stack, projectionMatrix, isFoggy, setupFog, bufferbuilder); // TODO Make the center one render last
		}
	}
}
