package net.povstalec.spacetravel.client.render.space_objects;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.povstalec.spacetravel.client.RenderCenter;
import net.povstalec.spacetravel.client.render.StarBuffer;
import net.povstalec.spacetravel.client.render.shaders.SpaceTravelShaders;
import net.povstalec.spacetravel.common.space.objects.AbstractStarField;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import net.povstalec.spacetravel.common.util.StarInfo;

public abstract class StarFieldRenderer<StarField extends AbstractStarField> extends SpaceObjectRenderer<AbstractStarField>
{
	@Nullable
	protected StarBuffer starBuffer;
	
	protected StarInfo starInfo;
	
	public StarFieldRenderer(StarField starField)
	{
		super(starField);
	}
	
	public boolean requiresSetup()
	{
		return starBuffer == null;
	}

	protected abstract BufferBuilder.RenderedBuffer generateStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords);
		
	protected abstract BufferBuilder.RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords);
	
	public void setStarBuffer(SpaceCoords relativeCoords)
	{
		if(starBuffer != null)
			starBuffer.close();
		
		starBuffer = new StarBuffer();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer;
		
		bufferbuilder$renderedbuffer = getStarBuffer(bufferBuilder, relativeCoords);
		
		starBuffer.bind();
		starBuffer.upload(bufferbuilder$renderedbuffer);
		VertexBuffer.unbind();
	}
	
	public void setupBuffer(SpaceCoords relativeCoords)
	{
		if(starBuffer != null)
			starBuffer.close();
		
		starBuffer = new StarBuffer();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer;
		
		bufferbuilder$renderedbuffer = generateStarBuffer(bufferBuilder, relativeCoords);
		
		starBuffer.bind();
		starBuffer.upload(bufferbuilder$renderedbuffer);
		VertexBuffer.unbind();
	}
	
	@Override
	public void render(RenderCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder, Vector3f parentVector)
	{
		if(requiresSetup())
			setupBuffer(viewCenter.getCoords().sub(spaceObject.getSpaceCoords()));
		else
			setStarBuffer(viewCenter.getCoords().sub(spaceObject.getSpaceCoords())); // This could be viable with fewer stars
		
		float starBrightness = getStarBrightness(level, camera, partialTicks);
		
		if(starBrightness > 0.0F)
		{
			stack.pushPose();
			
			//stack.translate(0, 0, 0);
			RenderSystem.setShaderColor(1, 1, 1, starBrightness);
			//RenderSystem.setShaderTexture(0, new ResourceLocation("textures/environment/sun.png"));
			FogRenderer.setupNoFog();
			
			this.starBuffer.bind();
			this.starBuffer.drawWithShader(stack.last().pose(), projectionMatrix, SpaceTravelShaders.starShader());
			//this.starBuffer.drawWithShader(stack.last().pose(), projectionMatrix, GameRenderer.getPositionColorTexShader());
			VertexBuffer.unbind();
			
			setupFog.run();
			stack.popPose();
		}
		
		for(SpaceObjectRenderer<?> child : clientChildren)
		{
			child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, parentVector);
		}
	}
	
	/**
	 * @param level The Level the Player is currently in
	 * @param camera Player Camera
	 * @param partialTicks
	 * @return
	 */
	public static float getStarBrightness(ClientLevel level, Camera camera, float partialTicks)
	{
		float rain = 1.0F - level.getRainLevel(partialTicks);
		float starBrightness = level.getStarBrightness(partialTicks) * rain;
		
		return starBrightness;
	}
}
