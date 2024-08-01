package net.povstalec.spacetravel.client.space_object;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.client.RenderableSpaceObject;
import net.povstalec.spacetravel.client.SpaceObjectRenderer;
import net.povstalec.spacetravel.client.ViewCenter;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;
import net.povstalec.spacetravel.common.util.AxisRotation;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import net.povstalec.spacetravel.common.util.SphericalCoords;
import net.povstalec.spacetravel.common.util.TextureLayer;

public class ClientSpaceObject extends SpaceObject implements RenderableSpaceObject
{
	@Nullable
	protected RenderableSpaceObject clientParent;

	protected ArrayList<RenderableSpaceObject> clientChildren = new ArrayList<RenderableSpaceObject>();
	
	public ClientSpaceObject(){}
	
	public ClientSpaceObject(Optional<String> parentName, SpaceCoords coords, AxisRotation axisRotation, List<TextureLayer> textureLayers)
	{
		super(parentName, coords, axisRotation, textureLayers);
	}

	@Override
	@Nullable
	public RenderableSpaceObject getParent()
	{
		return clientParent;
	}

	@Override
	public void setParent(RenderableSpaceObject parent)
	{
		this.clientParent = parent;
		
	}

	@Override
	public void setCoords(SpaceCoords coords)
	{
		this.coords = coords;
	}

	@Override
	public SpaceCoords getCoords()
	{
		return coords;
	}
	
	public Vector3f getPos(long ticks)
	{
		return getPosition(ticks);
	}
	
	@Override
	public void addChild(RenderableSpaceObject child)
	{
		if(child.getParent() != null)
		{
			SpaceTravel.LOGGER.error(this.toString() + " already has a parent");
			return;
		}
		
		this.clientChildren.add(child);
		child.setParent(this);
		child.setCoords(child.getCoords().add(this.coords));
		
		child.addCoordsToClientChildren(this.coords);
	}
	
	@Override
	public void addCoordsToClientChildren(SpaceCoords coords)
	{
		for(RenderableSpaceObject childOfChild : this.clientChildren)
		{
			childOfChild.setCoords(childOfChild.getCoords().add(coords));
			childOfChild.addCoordsToClientChildren(coords);
		}
	}
	
	//============================================================================================
	//*****************************************Rendering******************************************
	//============================================================================================
	
	protected void renderTextureLayers(BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, float sizeMultiplier, float brightness)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		
		for(TextureLayer textureLayer : getTextureLayers())
		{
			SpaceObjectRenderer.renderTextureLayer(textureLayer, bufferbuilder, lastMatrix, sphericalCoords, ticks, brightness, sizeMultiplier, 0);
		}
	}
	
	@Override
	public void render(ViewCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera, 
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder, 
			Vector3f parentVector)
	{
		long ticks = level.getDayTime();
		
		Vector3f positionVector = getPosition(ticks).add(parentVector); // Handles orbits 'n stuff
		
		if(!viewCenter.objectEquals(this))
		{
			// Add parent vector to current coords
			SpaceCoords coords = getSpaceCoords().add(positionVector);
			
			// Subtract coords of this from View Center coords to get relative coords
			SphericalCoords sphericalCoords = coords.skyPosition(viewCenter.getCoords());
			
			double distance = sphericalCoords.r;
			sphericalCoords.r = DEFAULT_DISTANCE;
			
			renderTextureLayers(bufferbuilder, stack.last().pose(), sphericalCoords, ticks, sizeMultiplier((float) distance), 1); //TODO Brightness
			
			// Render children in front of the parent
			for(RenderableSpaceObject child : clientChildren)
			{
				child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, positionVector);
			}
		}
	}
}
