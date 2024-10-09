package net.povstalec.spacetravel.client.render.space_objects;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.client.RenderCenter;
import net.povstalec.spacetravel.common.space.Spaceship;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;
import net.povstalec.spacetravel.common.space.objects.StarField;
import net.povstalec.spacetravel.common.space.objects.TexturedObject;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import net.povstalec.spacetravel.common.util.SphericalCoords;
import net.povstalec.spacetravel.common.util.TextureLayer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class TexturedObjectRenderer<TO extends TexturedObject> extends SpaceObjectRenderer<TexturedObject>
{
	public TexturedObjectRenderer(TO spaceObject)
	{
		super(spaceObject);
	}
	
	//============================================================================================
	//*****************************************Rendering******************************************
	//============================================================================================
	
	protected void renderTextureLayers(BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, float sizeMultiplier, float brightness)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		
		for(TextureLayer textureLayer : spaceObject.getTextureLayers())
		{
			TexturedObjectRenderer.renderTextureLayer(textureLayer, bufferbuilder, lastMatrix, sphericalCoords, ticks, brightness, sizeMultiplier, 0);
		}
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
			Vector3f parentVector)
	{
		long ticks = level.getDayTime();
		
		Vector3f positionVector = spaceObject.getPosition(ticks).add(parentVector); // Handles orbits 'n stuff
		
		if(!viewCenter.objectEquals(spaceObject))
		{
			// Add parent vector to current coords
			SpaceCoords coords = spaceObject.getSpaceCoords().add(positionVector);
			
			// Subtract coords of this from View Center coords to get relative coords
			SphericalCoords sphericalCoords = coords.skyPosition(viewCenter.getCoords());
			
			double distance = sphericalCoords.r;
			sphericalCoords.r = DEFAULT_DISTANCE;
			
			renderTextureLayers(bufferbuilder, stack.last().pose(), sphericalCoords, ticks, spaceObject.sizeMultiplier((float) distance), 1); //TODO Brightness
			
			// Render children in front of the parent
			for(SpaceObjectRenderer<?> child : clientChildren)
			{
				child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, positionVector);
			}
		}
	}
	
	/**
	 * Method used for rendering the sky from some Space Object's point of view
	 * @param viewCenter
	 * @param level
	 * @param partialTicks
	 * @param stack
	 * @param camera
	 * @param projectionMatrix
	 * @param isFoggy
	 * @param setupFog
	 * @param bufferbuilder
	 */
	public void renderFrom(RenderCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera, 
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		viewCenter.addCoords(spaceObject.getPosition(level.getDayTime()));
		
		if(parent != null)
			parent.renderFrom(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder);
		else
			viewCenter.renderSkyObjects(this, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder);
	}
	
	//============================================================================================
	//**********************************Texture Layer Rendering***********************************
	//============================================================================================
	
	public static void renderTextureLayer(TextureLayer textureLayer, BufferBuilder bufferbuilder,
			Matrix4f lastMatrix, SphericalCoords sphericalCoords,
			long ticks, float brightness, double mulSize, double addRotation)
	{
		if(brightness <= 0.0F || textureLayer.rgba().alpha() <= 0)
			return;
		
		float size = textureLayer.mulSize(mulSize);
		
		if(size < textureLayer.minSize())
		{
			if(textureLayer.clampAtMinSize())
				size = (float) textureLayer.minSize();
			else
				return;
		}
		
		float rotation = textureLayer.rotation(addRotation);
		//System.out.println(texture + " " + size);
		
		Vector3f corner00 = SphericalCoords.placeOnSphere(-size, -size, sphericalCoords, rotation);
		Vector3f corner10 = SphericalCoords.placeOnSphere(size, -size, sphericalCoords, rotation);
		Vector3f corner11 = SphericalCoords.placeOnSphere(size, size, sphericalCoords, rotation);
		Vector3f corner01 = SphericalCoords.placeOnSphere(-size, size, sphericalCoords, rotation);
	
	
		if(textureLayer.shoulBlend())
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		
		RenderSystem.setShaderColor(textureLayer.rgba().red() / 255F, textureLayer.rgba().green() / 255F, textureLayer.rgba().blue() / 255F, brightness * textureLayer.rgba().alpha() / 255F);
		
		RenderSystem.setShaderTexture(0, textureLayer.texture());
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        
        bufferbuilder.vertex(lastMatrix, corner00.x, corner00.y, corner00.z).uv(textureLayer.uv().topRight().u(ticks), textureLayer.uv().topRight().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner10.x, corner10.y, corner10.z).uv(textureLayer.uv().bottomRight().u(ticks), textureLayer.uv().bottomRight().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner11.x, corner11.y, corner11.z).uv(textureLayer.uv().bottomLeft().u(ticks), textureLayer.uv().bottomLeft().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner01.x, corner01.y, corner01.z).uv(textureLayer.uv().topLeft().u(ticks), textureLayer.uv().topLeft().v(ticks)).endVertex();
        
        BufferUploader.drawWithShader(bufferbuilder.end());
        
        RenderSystem.defaultBlendFunc();
	}
	
	public static void renderTextureLayer(TextureLayer textureLayer, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, float brightness)
	{
		renderTextureLayer(textureLayer, bufferbuilder, lastMatrix, sphericalCoords, ticks, brightness, 1, 0);
	}
	
	
	
	public static class Generic extends TexturedObjectRenderer<TexturedObject>
	{
		public Generic(TexturedObject spaceObject)
		{
			super(spaceObject);
		}
	}
}
