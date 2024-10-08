package net.povstalec.spacetravel.client;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import net.povstalec.spacetravel.client.effects.FogEffects;
import net.povstalec.spacetravel.client.effects.SkyEffects;
import net.povstalec.spacetravel.client.render.SpaceRenderer;
import net.povstalec.spacetravel.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;
import net.povstalec.spacetravel.common.util.AxisRotation;
import net.povstalec.spacetravel.common.util.SpaceCoords;

public class RenderCenter
{
	/*@Nullable
	private ResourceKey<RenderableSpaceObject> viewCenterKey;*/
	@Nullable
	public SpaceObjectRenderer<?> viewCenter;
	
	private Minecraft minecraft = Minecraft.getInstance();
	@Nullable
	private VertexBuffer skyBuffer = SkyEffects.createLightSky();
	@Nullable
	private VertexBuffer darkBuffer = SkyEffects.createDarkSky();
	
	private SpaceCoords coords;
	private AxisRotation axisRotation = new AxisRotation(); //TODO Is this really necessary? I'd say the viewCenter axis rotation could be used here instead
	
	public RenderCenter(/*Optional<ResourceKey<RenderableSpaceObject>> viewCenterKey*/)
	{
		//if(viewCenterKey.isPresent())
		//	this.viewCenterKey = viewCenterKey.get();
	}
	
	/*public boolean setViewCenterObject(HashMap<ResourceLocation, RenderableSpaceObject> spaceObjects)
	{
		if(viewCenterKey != null)
		{
			if(spaceObjects.containsKey(viewCenterKey.location()))
			{
				viewCenter = spaceObjects.get(viewCenterKey.location());
				return true;
			}
			
			SpaceTravel.LOGGER.error("Failed to register View Center because view center object " + viewCenterKey.location() + " could not be found");
			return false;
		}
		
		return true;
	}
	
	public Optional<ResourceKey<RenderableSpaceObject>> getViewCenterKey()
	{
		if(viewCenterKey != null)
			return Optional.of(viewCenterKey);
		
		return Optional.empty();
	}*/
	
	public AxisRotation getObjectAxisRotation()
	{
		if(viewCenter != null && viewCenter.spaceObject != null)
			return viewCenter.spaceObject.getAxisRotation();
		
		return new AxisRotation();
	}
	
	public SpaceCoords getCoords()
	{
		return coords;
	}
	
	public void addCoords(SpaceCoords other)
	{
		this.coords = this.coords.add(other);
	}
	
	public void addCoords(Vector3f vector)
	{
		this.coords = this.coords.add(vector);
	}
	
	public void subCoords(SpaceCoords other)
	{
		this.coords = this.coords.sub(other);
	}
	
	public AxisRotation getAxisRotation()
	{
		return axisRotation;
	}
	
	public boolean objectEquals(SpaceObject spaceObject)
	{
		if(this.viewCenter != null)
			return spaceObject == this.viewCenter.spaceObject;
		
		return false;
	}
	
	private boolean renderSkyObjectsFrom(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		if(viewCenter == null)
			return false;
		
		coords = viewCenter.spaceObject.getSpaceCoords();
		
		stack.pushPose();
		
		stack.mulPose(Axis.YP.rotation((float) viewCenter.spaceObject.getAxisRotation().yAxis())); //TODO Rotation of the sky depending on where you are
		stack.mulPose(Axis.ZP.rotation((float) viewCenter.spaceObject.getAxisRotation().zAxis())); //TODO Rotation of the sky because you're on the surface
		stack.mulPose(Axis.XP.rotation((float) viewCenter.spaceObject.getAxisRotation().xAxis())); //TODO Rotation of the planet
		
		viewCenter.renderFrom(this, level, partialTicks, stack, camera, projectionMatrix, FogEffects.isFoggy(minecraft, camera), setupFog, bufferbuilder);

		stack.popPose();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		return true;
	}
	
	public void renderSkyObjects(SpaceObjectRenderer<?> masterParent, ClientLevel level, float partialTicks, PoseStack stack, Camera camera, 
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		SpaceRenderer.render(this, masterParent, level, camera, partialTicks, stack, projectionMatrix, isFoggy, setupFog, bufferbuilder);
	}
	
	public boolean renderSky(ClientLevel level, int ticks, float partialTicks, PoseStack stack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
	{
		if(viewCenter == null)
			return false;
		
		setupFog.run();
		
		if(!FogEffects.isFoggy(this.minecraft, camera))
		{
			Vec3 skyColor = level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), partialTicks);
			float skyX = (float)skyColor.x;
	        float skyY = (float)skyColor.y;
	        float skyZ = (float)skyColor.z;
	        FogRenderer.levelFogColor();
			BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
			RenderSystem.depthMask(false);
			RenderSystem.setShaderColor(skyX, skyY, skyZ, 1.0F);
			ShaderInstance shaderinstance = RenderSystem.getShader();
			this.skyBuffer.bind();
			this.skyBuffer.drawWithShader(stack.last().pose(), projectionMatrix, shaderinstance);
			VertexBuffer.unbind();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			
			SkyEffects.renderSunrise(level, partialTicks, stack, projectionMatrix, bufferbuilder);

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			
			renderSkyObjectsFrom(level, camera, partialTicks, stack, projectionMatrix, setupFog, bufferbuilder);
	        
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	        RenderSystem.disableBlend();
	        
	        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
	        double height = this.minecraft.player.getEyePosition(partialTicks).y - level.getLevelData().getHorizonHeight(level);
	        if(height < 0.0D)
	        {
	        	stack.pushPose();
	        	stack.translate(0.0F, 12.0F, 0.0F);
	        	this.darkBuffer.bind();
	        	this.darkBuffer.drawWithShader(stack.last().pose(), projectionMatrix, shaderinstance);
	        	VertexBuffer.unbind();
	        	stack.popPose();
	        }
	        
	        if(level.effects().hasGround())
	        	RenderSystem.setShaderColor(skyX * 0.2F + 0.04F, skyY * 0.2F + 0.04F, skyZ * 0.6F + 0.1F, 1.0F);
	        else
	        	RenderSystem.setShaderColor(skyX, skyY, skyZ, 1.0F);
	        
	        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	        RenderSystem.depthMask(true);
		}
		
		return true;
	}
	
	
}
