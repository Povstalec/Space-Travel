package net.povstalec.spacetravel.client.render.space_objects;

import java.util.ArrayList;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.client.RenderCenter;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import net.povstalec.spacetravel.common.util.SphericalCoords;
import net.povstalec.spacetravel.common.util.TextureLayer;

public abstract class SpaceObjectRenderer<RenderedSpaceObject extends SpaceObject>
{
	public static final float DEFAULT_DISTANCE = 100.0F;
	
	public final RenderedSpaceObject spaceObject;
	
	@Nullable
	protected SpaceObjectRenderer<?> parent;

	protected ArrayList<SpaceObjectRenderer<?>> clientChildren = new ArrayList<SpaceObjectRenderer<?>>();
	
	public SpaceObjectRenderer(RenderedSpaceObject spaceObject)
	{
		this.spaceObject = spaceObject;
	}
	
	public void addChild(SpaceObjectRenderer<?> child)
	{
		if(child.parent != null)
		{
			SpaceTravel.LOGGER.error(this.toString() + " already has a parent");
			return;
		}
		
		this.clientChildren.add(child);
		child.parent = this;
		child.spaceObject.addSpaceCoords(this.spaceObject.getSpaceCoords());
		
		child.addCoordsToClientChildren(this.spaceObject.getSpaceCoords());
	}
	
	public void addCoordsToClientChildren(SpaceCoords coords)
	{
		for(SpaceObjectRenderer<?> childOfChild : this.clientChildren)
		{
			childOfChild.spaceObject.addSpaceCoords(coords);
			childOfChild.addCoordsToClientChildren(coords);
		}
	}
	
	//============================================================================================
	//*****************************************Rendering******************************************
	//============================================================================================
	
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
	public abstract void render(RenderCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera,
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
	public void renderFrom(RenderCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera,
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		viewCenter.addCoords(spaceObject.getPosition(level.getDayTime()));
		
		if(parent != null)
			parent.renderFrom(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder);
		else
			viewCenter.renderSkyObjects(this, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder);
	}
}
