package net.povstalec.spacetravel.client.space_object;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.client.RenderCenter;
import net.povstalec.spacetravel.client.render.StarBuffer;
import net.povstalec.spacetravel.client.render.shaders.SpaceTravelShaders;
import net.povstalec.spacetravel.common.space.objects.Galaxy.SpiralGalaxy;
import net.povstalec.spacetravel.common.space.objects.StarField;
import net.povstalec.spacetravel.common.util.AxisRotation;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import net.povstalec.spacetravel.common.util.SphericalCoords;
import net.povstalec.spacetravel.common.util.StarInfo;
import net.povstalec.spacetravel.common.util.TextureLayer;

public class ClientGalaxy
{
	public static class ClientSpiralGalaxy extends SpiralGalaxy implements RenderableSpaceObject
	{
		@Nullable
		protected StarBuffer starBuffer;
		
		@Nullable
		protected RenderableSpaceObject clientParent;

		protected ArrayList<RenderableSpaceObject> clientChildren = new ArrayList<RenderableSpaceObject>();
		
		public ClientSpiralGalaxy() {}
		
		public ClientSpiralGalaxy(ResourceLocation objectType, Optional<String> parentName, SpaceCoords coords, AxisRotation axisRotation,
				List<TextureLayer> textureLayers, long seed, int diameter, int numberOfArms, double armThickness, int starsPerArm)
		{
			super(objectType, parentName, coords, axisRotation, textureLayers, seed, diameter, numberOfArms, armThickness, starsPerArm);
		}
		
		public boolean requiresSetup()
		{
			return starBuffer == null;
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
		
		protected RenderedBuffer generateStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			
			double spread = armThickness;
			double sizeMultiplier = diameter / 30D;
			
			starInfo = new StarInfo(stars * numberOfArms);
			
			for(int j = 0; j < numberOfArms; j++) //Draw each arm
			{
				double rotation = Math.PI * j / ((double) numberOfArms / 2);
				double length = randomsource.nextDouble() + 1.5;
				for(int i = 0; i < stars; i++)
				{
					// Milky Way is 90 000 ly across
					
					double progress = (double) i / stars;
					
					double phi = length * Math.PI * progress - rotation;
					double r = spiralR(5, phi, rotation);

					Vector3d cartesian = new SphericalCoords(randomsource.nextDouble() * spread, randomsource.nextDouble() * 2F * Math.PI, randomsource.nextDouble() * Math.PI).toCartesianD();
					
					double x =  r * Math.cos(phi) + cartesian.x * spread / (progress * 1.5);
					double z =  r * Math.sin(phi) + cartesian.z * spread / (progress * 1.5);
					double y =  cartesian.y * spread / (progress * 1.5);
					
					x *= sizeMultiplier;
					y *= sizeMultiplier;
					z *= sizeMultiplier;
					
					//Rotates around X
					double alphaX = x;
					double alphaY = z * Math.sin(axisRotation.xAxis()) + y * Math.cos(axisRotation.xAxis());
					double alphaZ = z * Math.cos(axisRotation.xAxis()) - y * Math.sin(axisRotation.xAxis());
					
					//Rotates around Z
					double betaX = alphaX * Math.cos(axisRotation.zAxis()) - alphaY * Math.sin(axisRotation.zAxis());
					double betaY = - alphaX * Math.sin(axisRotation.zAxis()) - alphaY * Math.cos(axisRotation.zAxis());
					double betaZ = alphaZ;
					
					//Rotates around Y
					double gammaX = - betaX * Math.sin(axisRotation.yAxis()) - betaZ * Math.cos(axisRotation.yAxis());
					double gammaY = betaY;
					double gammaZ = betaX * Math.cos(axisRotation.yAxis()) - betaZ * Math.sin(axisRotation.yAxis());
					
					starInfo.newStar(bufferBuilder, randomsource, relativeCoords, gammaX, gammaY, gammaZ, j * stars + i);
				}
			}
			return bufferBuilder.end();
		}
		
		protected RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			
			for(int j = 0; j < numberOfArms; j++) //Draw each arm
			{
				for(int i = 0; i < stars; i++)
				{
					starInfo.createStar(bufferBuilder, randomsource, relativeCoords, j * stars + i);
				}
			}
			return bufferBuilder.end();
		}
		
		public StarField setStarBuffer(SpaceCoords relativeCoords)
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
			
			return this;
		}
		
		public StarField setupBuffer(SpaceCoords relativeCoords)
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
			
			return this;
		}
		
		@Override
		public void render(RenderCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder, Vector3f parentVector)
		{
			if(requiresSetup())
				setupBuffer(viewCenter.getCoords().sub(this.coords));
			//else
			//	setStarBuffer(viewCenter.getCoords().sub(this.coords)); // This could be viable with fewer stars
			
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
			
			for(RenderableSpaceObject child : clientChildren)
			{
				child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, parentVector);
			}
		}
	}
	
	public static double spiralR(double r, double phi, double beta)
	{
		return r * (phi + beta);
	}
}
