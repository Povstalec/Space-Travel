package net.povstalec.spacetravel.client.render.space_objects;

import org.joml.Vector3d;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.util.RandomSource;
import net.povstalec.spacetravel.common.space.objects.Galaxy;
import net.povstalec.spacetravel.common.util.AxisRotation;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import net.povstalec.spacetravel.common.util.SphericalCoords;
import net.povstalec.spacetravel.common.util.StarInfo;

public class GalaxyRenderer
{
	public static double spiralR(double r, double phi, double beta)
	{
		return r * (phi + beta);
	}
	
	public static class SpiralGalaxy extends StarFieldRenderer<Galaxy.SpiralGalaxy>
	{
		protected double armThickness;
		protected short numberOfArms;
		
		public SpiralGalaxy(Galaxy.SpiralGalaxy spiralGalaxy)
		{
			super(spiralGalaxy);
			this.armThickness = spiralGalaxy.getArmThickness();
			this.numberOfArms = spiralGalaxy.getNumberOfArms();
		}
		
		protected RenderedBuffer generateStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
		{
			RandomSource randomsource = RandomSource.create(spaceObject.getSeed());
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			
			double spread = armThickness;
			double sizeMultiplier = spaceObject.getDiameter() / 30D;
			
			int starsPerArm = spaceObject.getStars();
			
			starInfo = new StarInfo(starsPerArm * numberOfArms);
			
			for(int j = 0; j < numberOfArms; j++) //Draw each arm
			{
				double rotation = Math.PI * j / ((double) numberOfArms / 2);
				double length = randomsource.nextDouble() + 1.5;
				for(int i = 0; i < starsPerArm; i++)
				{
					// Milky Way is 90 000 ly across
					
					double progress = (double) i / starsPerArm;
					
					double phi = length * Math.PI * progress - rotation;
					double r = spiralR(5, phi, rotation);

					Vector3d cartesian = new SphericalCoords(randomsource.nextDouble() * spread, randomsource.nextDouble() * 2F * Math.PI, randomsource.nextDouble() * Math.PI).toCartesianD();
					
					double x =  r * Math.cos(phi) + cartesian.x * spread / (progress * 1.5);
					double z =  r * Math.sin(phi) + cartesian.z * spread / (progress * 1.5);
					double y =  cartesian.y * spread / (progress * 1.5);
					
					x *= sizeMultiplier;
					y *= sizeMultiplier;
					z *= sizeMultiplier;
					
					AxisRotation axisRotation = spaceObject.getAxisRotation();
					
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
					
					starInfo.newStar(bufferBuilder, randomsource, relativeCoords, gammaX, gammaY, gammaZ, j * starsPerArm + i);
				}
			}
			return bufferBuilder.end();
		}
		
		protected RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
		{
			RandomSource randomsource = RandomSource.create(spaceObject.getSeed());
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			
			for(int j = 0; j < numberOfArms; j++) //Draw each arm
			{
				for(int i = 0; i < spaceObject.getStars(); i++)
				{
					starInfo.createStar(bufferBuilder, randomsource, relativeCoords, j * spaceObject.getStars() + i);
				}
			}
			return bufferBuilder.end();
		}
		
	}
}
