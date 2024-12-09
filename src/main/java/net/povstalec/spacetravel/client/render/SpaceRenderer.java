package net.povstalec.spacetravel.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.povstalec.spacetravel.client.render.space_objects.BlackHoleRenderer;
import net.povstalec.spacetravel.common.config.SpaceRegionClientConfig;
import net.povstalec.spacetravel.common.config.StarFieldClientConfig;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.spacetravel.client.RenderCenter;
import net.povstalec.spacetravel.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.spacetravel.common.space.SpaceRegion;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SpaceRenderer
{
	private static int range = setupRange();
	
	private static float maxRenderDistance = setupMaxRenderDistance();
	private static float fadeStartDistance = setupFadeStartDistance();
	private static float fade = maxRenderDistance - fadeStartDistance;
	
	public static final Matrix3f IDENTITY_MATRIX = new Matrix3f();
	
	public static Matrix3f lensingMatrix = IDENTITY_MATRIX;
	public static Matrix3f lensingMatrixInv = IDENTITY_MATRIX;
	public static float lensingIntensity = 0;
	
	private static final HashMap<SpaceRegion.Position, ClientSpaceRegion> SPACE_REGIONS = new HashMap<SpaceRegion.Position, ClientSpaceRegion>();
	
	public static RenderCenter renderCenter;
	
	private static boolean isUpToDate()
	{
		return range == SpaceRegionClientConfig.space_region_render_distance.get();
	}
	
	private static int setupRange()
	{
		return SpaceRegionClientConfig.space_region_render_distance.get() <= SpaceRegion.SPACE_REGION_LOAD_DISTANCE ?
				SpaceRegionClientConfig.space_region_render_distance.get() : SpaceRegion.SPACE_REGION_LOAD_DISTANCE;
	}
	
	private static float setupMaxRenderDistance()
	{
		long regionRange = SpaceRegion.LY_PER_REGION * (range - 1);
		
		return (float) (regionRange * regionRange);
	}
	private static float setupFadeStartDistance()
	{
		long regionRange = SpaceRegion.LY_PER_REGION * (range - 2);
		
		return (float) (regionRange * regionRange);
	}
	private static void updateRange()
	{
		range = setupRange();
		
		maxRenderDistance = setupFadeStartDistance();
		fadeStartDistance = setupFadeStartDistance();
		fade = maxRenderDistance - fadeStartDistance;
	}
	
	public static int getRange()
	{
		if(!isUpToDate())
			updateRange();
		
		return range;
	}
	
	public static float getMaxRenderDistance()
	{
		if(!isUpToDate())
			updateRange();
		
		return maxRenderDistance;
	}
	
	public static float getFadeStartDistance()
	{
		if(!isUpToDate())
			updateRange();
		
		return fadeStartDistance;
	}
	
	public static float getFade()
	{
		if(!isUpToDate())
			updateRange();
		
		return fade;
	}
	
	
	
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
	
	public static void render(RenderCenter renderCenter, SpaceObjectRenderer<?> masterParent, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		setBestLensing();
		
		SpaceRegion.Position pos = new SpaceRegion.Position(renderCenter.getCoords());
		
		if(StarFieldClientConfig.dust_clouds.get())
		{
			float dustCloudBrightness = 1F;//StarField.dustCloudBrightness(viewCenter, level, camera, partialTicks);
			for(Map.Entry<SpaceRegion.Position, ClientSpaceRegion> spaceRegionEntry : SPACE_REGIONS.entrySet())
			{
				spaceRegionEntry.getValue().renderDustClouds(renderCenter, level, camera, partialTicks, stack, projectionMatrix, setupFog, dustCloudBrightness);
			}
		}
		
		ClientSpaceRegion centerRegion = null;
		for(Map.Entry<SpaceRegion.Position, ClientSpaceRegion> spaceRegionEntry : SPACE_REGIONS.entrySet())
		{
			if(!spaceRegionEntry.getKey().equals(pos))
			{
				if(spaceRegionEntry.getKey().isInRange(pos, getRange()))
					spaceRegionEntry.getValue().render(renderCenter, masterParent, level, camera, partialTicks, stack, projectionMatrix, isFoggy, setupFog, bufferbuilder);
			}
			else
				centerRegion = spaceRegionEntry.getValue();
		}
		
		if(centerRegion != null)
			centerRegion.render(renderCenter, masterParent, level, camera, partialTicks, stack, projectionMatrix, isFoggy, setupFog, bufferbuilder);
	}
	
	
	
	private static void setBestLensing()
	{
		SpaceRenderer.lensingMatrix = SpaceRenderer.IDENTITY_MATRIX;
		SpaceRenderer.lensingMatrixInv = SpaceRenderer.IDENTITY_MATRIX;
		SpaceRenderer.lensingIntensity = 0;
		
		for(Map.Entry<SpaceRegion.Position, ClientSpaceRegion> spaceRegionEntry : SPACE_REGIONS.entrySet())
		{
			spaceRegionEntry.getValue().setBestLensing();
		}
	}
}
