package net.povstalec.spacetravel.client.render.level;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.render.ViewCenters;
import net.povstalec.stellarview.client.render.space_objects.ViewObjectRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.AxisRotation;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Optional;

public class PlanetSpecialEffects extends SpaceTravelDimensionSpecialEffects
{
	private static final HashMap<ResourceLocation, ViewCenter> VIEW_CENTER_MAP = new HashMap();
	
	public PlanetSpecialEffects()
	{
		super(Float.NaN, false, DimensionSpecialEffects.SkyType.NONE, false, false);
	}
	
	@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
	{
		if(!VIEW_CENTER_MAP.containsKey(level.dimension().location()))
			return false;
		
		return VIEW_CENTER_MAP.get(level.dimension().location()).renderSky(level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
	}
	
	public static void createPlanetViewCenter(ResourceLocation dimension, ViewObjectRenderer<?> viewObjectRenderer)
	{
		ViewCenter viewCenter = new ViewCenter(Optional.empty(), Optional.empty(), new AxisRotation(0, -90, -90), 24000,
				ViewCenter.DayBlending.DAY_BLENDING, ViewCenter.DayBlending.SUN_DAY_BLENDING,
				Optional.empty(), Optional.empty(), true, true, false, false, true, 30000000);
		viewCenter.setViewObjectRenderer(viewObjectRenderer);
		
		VIEW_CENTER_MAP.put(dimension, viewCenter);
	}
}
