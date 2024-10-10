package net.povstalec.spacetravel.client.render.space_objects;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.client.RenderCenter;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;
import net.povstalec.spacetravel.common.space.objects.TexturedObject;
import net.povstalec.spacetravel.common.util.*;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Vector3f;

public class TexturedObjectRenderer<TO extends TexturedObject> extends SpaceObjectRenderer<TO>
{
	public TexturedObjectRenderer(TO spaceObject)
	{
		super(spaceObject);
	}
	
	//============================================================================================
	//*****************************************Rendering******************************************
	//============================================================================================
	
	
	public static void renderOnSphere(Color.FloatRGBA rgba, Color.FloatRGBA secondaryRGBA, ResourceLocation texture, UV.Quad uv,
									  ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords,
									  long ticks, double distance, float partialTicks, float brightness, float size, float rotation, boolean shouldBlend)
	{
		Vector3f corner00 = new Vector3f(size, DEFAULT_DISTANCE, size);
		Vector3f corner10 = new Vector3f(-size, DEFAULT_DISTANCE, size);
		Vector3f corner11 = new Vector3f(-size, DEFAULT_DISTANCE, -size);
		Vector3f corner01 = new Vector3f(size, DEFAULT_DISTANCE, -size);
		
		Quaterniond quaternionX = new Quaterniond().rotateY(sphericalCoords.theta);
		quaternionX.mul(new Quaterniond().rotateX(sphericalCoords.phi));
		quaternionX.mul(new Quaterniond().rotateY(rotation));
		
		quaternionX.transform(corner00);
		quaternionX.transform(corner10);
		quaternionX.transform(corner11);
		quaternionX.transform(corner01);
		
		if(shouldBlend)
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		else
			RenderSystem.defaultBlendFunc();
		
		RenderSystem.setShaderColor(rgba.red() * secondaryRGBA.red(), rgba.green() * secondaryRGBA.green(), rgba.blue() * secondaryRGBA.blue(), brightness * rgba.alpha() * secondaryRGBA.alpha());
		
		RenderSystem.setShaderTexture(0, texture);
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		
		bufferbuilder.vertex(lastMatrix, corner00.x, corner00.y, corner00.z).uv(uv.topRight().u(ticks), uv.topRight().v(ticks)).endVertex();
		bufferbuilder.vertex(lastMatrix, corner10.x, corner10.y, corner10.z).uv(uv.bottomRight().u(ticks), uv.bottomRight().v(ticks)).endVertex();
		bufferbuilder.vertex(lastMatrix, corner11.x, corner11.y, corner11.z).uv(uv.bottomLeft().u(ticks), uv.bottomLeft().v(ticks)).endVertex();
		bufferbuilder.vertex(lastMatrix, corner01.x, corner01.y, corner01.z).uv(uv.topLeft().u(ticks), uv.topLeft().v(ticks)).endVertex();
		
		BufferUploader.drawWithShader(bufferbuilder.end());
		
		RenderSystem.defaultBlendFunc();
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
		SphericalCoords sphericalCoords = coords.skyPosition(viewCenter.getCoords());
		
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
				if(child.spaceObject.lastDistance >= spaceObject.lastDistance)
					child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, positionVector, spaceObject.getAxisRotation());
			}
		}
	}
	
	public static float dayBrightness(RenderCenter viewCenter, float size, long ticks, ClientLevel level, Camera camera, float partialTicks)
	{
		return 1; //TODO Make this more adjustable
	}
	
	//============================================================================================
	//**********************************Texture Layer Rendering***********************************
	//============================================================================================
	
	protected void renderTextureLayers(RenderCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		
		for(TextureLayer textureLayer : spaceObject.getTextureLayers())
		{
			renderTextureLayer(textureLayer, viewCenter, level, camera, bufferbuilder, lastMatrix, sphericalCoords, ticks, distance, partialTicks);
		}
	}
	
	/**
	 * Method for rendering an individual texture layer, override to change details of how this object's texture layers are rendered
	 * @param textureLayer
	 * @param level
	 * @param bufferbuilder
	 * @param lastMatrix
	 * @param sphericalCoords
	 * @param ticks
	 * @param distance
	 * @param partialTicks
	 */
	protected void renderTextureLayer(TextureLayer textureLayer, RenderCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
	{
		if(textureLayer.rgba().alpha() <= 0)
			return;
		
		float size = (float) textureLayer.mulSize(SpaceObject.distanceSize(distance));
		
		if(size < textureLayer.minSize())
		{
			if(textureLayer.clampAtMinSize())
				size = (float) textureLayer.minSize();
			else
				return;
		}
		
		renderOnSphere(textureLayer.rgba(), Color.FloatRGBA.DEFAULT, textureLayer.texture(), textureLayer.uv(),
				level, camera, bufferbuilder, lastMatrix, sphericalCoords,
				ticks, distance, partialTicks, dayBrightness(viewCenter, size, ticks, level, camera, partialTicks), size, (float) textureLayer.rotation(), textureLayer.shoulBlend());
	}
	
	
	
	public static class Generic extends TexturedObjectRenderer<TexturedObject>
	{
		public Generic(TexturedObject spaceObject)
		{
			super(spaceObject);
		}
	}
}
