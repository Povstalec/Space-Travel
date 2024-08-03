package net.povstalec.spacetravel.client.space_object;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.spacetravel.client.RenderCenter;
import net.povstalec.spacetravel.common.util.SpaceCoords;

public interface RenderableSpaceObject
{
	@Nullable
	public RenderableSpaceObject getParent();
	
	public void setParent(RenderableSpaceObject parent);
	
	public void addChild(RenderableSpaceObject child);
	
	public SpaceCoords getCoords();
	
	public void setCoords(SpaceCoords coords);
	
	public void addCoordsToClientChildren(SpaceCoords coords);
	
	public Vector3f getPos(long ticks);
	
	public boolean isInitialized();
	
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
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder, Vector3f parentVector);
	
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
	public default void renderFrom(RenderCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera, 
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		viewCenter.addCoords(getPos(level.getDayTime()));
		
		if(getParent() != null)
			getParent().renderFrom(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder);
		else
			viewCenter.renderSkyObjects(this, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder);
	}
}
