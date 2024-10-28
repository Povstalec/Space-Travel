package net.povstalec.spacetravel.client.render.space_objects;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.client.RenderCenter;
import net.povstalec.spacetravel.client.render.SpaceRenderer;
import net.povstalec.spacetravel.common.space.objects.BlackHole;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;
import net.povstalec.spacetravel.common.space.objects.Star;
import net.povstalec.spacetravel.common.util.*;
import org.joml.*;

public class BlackHoleRenderer extends TexturedObjectRenderer<BlackHole>
{
	protected SphericalCoords sphericalCoords = new SphericalCoords(0, 0, 0);
	
	public BlackHoleRenderer(BlackHole blackHole)
	{
		super(blackHole);
	}
	
	public void setupLensing()
	{
		float intensity = (float) spaceObject.getLensingIntensity(spaceObject.lastDistance);
		
		if(intensity < SpaceRenderer.lensingIntensity)
			return;
		
		Quaternionf lensingQuat = new Quaternionf().rotateY((float) sphericalCoords.theta);
		lensingQuat.mul(new Quaternionf().rotateX((float) sphericalCoords.phi));
		
		Matrix3f lensingMatrixInv = new Matrix3f().rotate(lensingQuat);
		Matrix3f lensingMatrix = new Matrix3f().rotate(lensingQuat.invert());
		
		SpaceRenderer.lensingIntensity = intensity;
		SpaceRenderer.lensingMatrixInv = lensingMatrixInv;
		SpaceRenderer.lensingMatrix = lensingMatrix;
	}
	
	//============================================================================================
	//*****************************************Rendering******************************************
	//============================================================================================
	
	@Override
	protected void renderTextureLayer(TextureLayer textureLayer, RenderCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
	{
		double lyDistance = distance / SpaceCoords.LY_TO_KM;
		
		Color.FloatRGBA starRGBA = spaceObject.starRGBA(lyDistance);
		
		if(starRGBA.alpha() <= 0.0F || textureLayer.rgba().alpha() <= 0)
			return;
		
		float size = (float) textureLayer.mulSize(SpaceObject.distanceSize(distance));
		
		if(size < textureLayer.minSize())
		{
			if(textureLayer.clampAtMinSize())
			{
				size = (float) textureLayer.minSize();
				
				// Once the star has reached its usual min size, it will start getting smaller slowly again, but only up to a certain point
				size = spaceObject.starSize(size, lyDistance);
			}
			else
				return;
		}
		
		renderOnSphere(textureLayer.rgba(), starRGBA, textureLayer.texture(), textureLayer.uv(),
				level, camera, bufferbuilder, lastMatrix, sphericalCoords,
				ticks, distance, partialTicks, dayBrightness(viewCenter, size, ticks, level, camera, partialTicks), size, (float) textureLayer.rotation(), textureLayer.shoulBlend());
	}
	
	/**
	 * Method for rendering a Space Object in the sky
	 * @param viewCenter
	 * @param level
	 * @param partialTicks
	 * @param stack
	 * @param camera
	 * @param projectionMatrix
	 * @param isFoggy
	 * @param setupFog
	 * @param bufferbuilder
	 * @param parentVector
	 */
	@Override
	public void render(RenderCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera,
					   Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder,
					   Vector3f parentVector, AxisRotation parentRotation)
	{
		long ticks = level.getDayTime();
		
		Vector3f positionVector = spaceObject
				.getPosition(!viewCenter.objectEquals(spaceObject), parentRotation, ticks, partialTicks)
				.add(parentVector); // Handles orbits 'n stuff
		
		boolean isViewCenter = viewCenter.objectEquals(spaceObject);
		
		// Add parent vector to current coords
		SpaceCoords coords = spaceObject.getSpaceCoords().add(positionVector);
		
		// Subtract coords of this from View Center coords to get relative coords
		sphericalCoords = coords.skyPosition(viewCenter.getCoords());
		
		spaceObject.lastDistance = sphericalCoords.r;
		sphericalCoords.r = DEFAULT_DISTANCE;
		
		if(spaceObject.getFadeOutHandler().getMaxChildRenderDistance().toKm() > spaceObject.lastDistance)
		{
			for(SpaceObjectRenderer<?> child : clientChildren)
			{
				// Render child behind the parent
				if(child.spaceObject.lastDistance >= spaceObject.lastDistance)
					child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, positionVector, spaceObject.getAxisRotation());
			}
		}
		
		if(!isViewCenter && spaceObject.getFadeOutHandler().getFadeOutEndDistance().toKm() > spaceObject.lastDistance)
			renderTextureLayers(viewCenter, level, camera, bufferbuilder, stack.last().pose(), sphericalCoords, ticks, spaceObject.lastDistance, partialTicks);
		
		if(spaceObject.getFadeOutHandler().getMaxChildRenderDistance().toKm() > spaceObject.lastDistance)
		{
			for(SpaceObjectRenderer<?> child : clientChildren)
			{
				// Render child in front of the parent
				if(child.spaceObject.lastDistance < spaceObject.lastDistance)
					child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, positionVector, spaceObject.getAxisRotation());
			}
		}
	}
}
