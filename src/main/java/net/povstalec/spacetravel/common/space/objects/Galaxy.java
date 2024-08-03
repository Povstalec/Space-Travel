package net.povstalec.spacetravel.common.space.objects;

import java.util.List;
import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.util.AxisRotation;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import net.povstalec.spacetravel.common.util.TextureLayer;

public class Galaxy
{
	public static class SpiralGalaxy extends StarField
	{
		public static final ResourceLocation SPIRAL_GALAXY_LOCATION = new ResourceLocation(SpaceTravel.MODID, "spiral_galaxy");
		
		public static final String ARM_THICKNESS = "arm_thickness";
		public static final String NUMBER_OF_ARMS = "number_of_arms";
		
		protected double armThickness;
		protected short numberOfArms;
		
		/*public static final Codec<SpiralGalaxy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(SpiralGalaxy::getParentKey),
				SpaceCoords.CODEC.fieldOf("coords").forGetter(SpiralGalaxy::getCoords),
				AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(SpiralGalaxy::getAxisRotation),
				TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(SpiralGalaxy::getTextureLayers),
				
				Codec.LONG.fieldOf("seed").forGetter(SpiralGalaxy::getSeed),
				Codec.INT.fieldOf("diameter_ly").forGetter(SpiralGalaxy::getDiameter),
				
				Codec.intRange(1, 8).fieldOf("number_of_arms").forGetter(SpiralGalaxy::getNumberOfArms),
				Codec.DOUBLE.fieldOf("arm_thickness").forGetter(SpiralGalaxy::getArmThickness),
				Codec.intRange(1, 30000).fieldOf("stars_per_arm").forGetter(SpiralGalaxy::getStars)
				).apply(instance, SpiralGalaxy::new));*/
		
		public SpiralGalaxy() {}
		
		public SpiralGalaxy(ResourceLocation objectType, Optional<String> parentName, SpaceCoords coords, AxisRotation axisRotation,
				List<TextureLayer> textureLayers, long seed, int diameter, int numberOfArms, double armThickness, int starsPerArm)
		{
			super(objectType, parentName, coords, axisRotation, textureLayers, seed, diameter, starsPerArm);
			
			this.numberOfArms = (short) numberOfArms;
			this.armThickness = armThickness;
		}
		
		public int getNumberOfArms()
		{
			return numberOfArms;
		}
		
		public double getArmThickness()
		{
			return armThickness;
		}

		@Override
		public CompoundTag serializeNBT()
		{
			CompoundTag tag = super.serializeNBT();
			
			tag.putDouble(ARM_THICKNESS, armThickness);
			tag.putShort(NUMBER_OF_ARMS, numberOfArms);
			
			return tag;
		}

		@Override
		public void deserializeNBT(CompoundTag tag)
		{
			super.deserializeNBT(tag);
			
			armThickness = tag.getDouble(ARM_THICKNESS);
			numberOfArms = tag.getShort(NUMBER_OF_ARMS);
		}
	}
	
	/*public static class EllipticalGalaxy extends StarField
	{
		private final double xStretch;
		private final double yStretch;
		private final double zStretch;
		
		public static final Codec<EllipticalGalaxy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(EllipticalGalaxy::getParentKey),
				SpaceCoords.CODEC.fieldOf("coords").forGetter(EllipticalGalaxy::getCoords),
				AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(EllipticalGalaxy::getAxisRotation),
				TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(EllipticalGalaxy::getTextureLayers),
				
				Codec.LONG.fieldOf("seed").forGetter(EllipticalGalaxy::getSeed),
				Codec.INT.fieldOf("diameter_ly").forGetter(EllipticalGalaxy::getDiameter),
				
				Codec.INT.fieldOf("stars").forGetter(EllipticalGalaxy::getStars),
				
				Codec.DOUBLE.fieldOf("x_stretch").forGetter(EllipticalGalaxy::xStretch),
				Codec.DOUBLE.fieldOf("y_stretch").forGetter(EllipticalGalaxy::yStretch),
				Codec.DOUBLE.fieldOf("z_stretch").forGetter(EllipticalGalaxy::zStretch)
				).apply(instance, EllipticalGalaxy::new));
		
		public EllipticalGalaxy(Optional<String> parentName, SpaceCoords coords, AxisRotation axisRotation,
				List<TextureLayer> textureLayers, long seed, int diameter, int starsPerArm, double xStretch, double yStretch, double zStretch)
		{
			super(parentName, coords, axisRotation, textureLayers, seed, diameter, starsPerArm);
			
			this.xStretch = xStretch;
			this.yStretch = yStretch;
			this.zStretch = zStretch;
		}
		
		public double xStretch()
		{
			return xStretch;
		}
		
		public double yStretch()
		{
			return yStretch;
		}
		
		public double zStretch()
		{
			return zStretch;
		}

		protected RenderedBuffer generateStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			
			starInfo = new StarInfo(stars);
			
			for(int i = 0; i < stars; i++)
			{
				// This generates random coordinates for the Star close to the camera
				Vector3d cartesian = new SphericalCoords(randomsource.nextDouble() * diameter, randomsource.nextDouble() * 2F * Math.PI, randomsource.nextDouble() * Math.PI).toCartesianD();
				
				cartesian.x *= xStretch;
				cartesian.y *= yStretch;
				cartesian.z *= zStretch;
				
				//Rotates around X
				double alphaX = cartesian.x;
				double alphaY = cartesian.z * Math.sin(axisRotation.xAxis()) + cartesian.y * Math.cos(axisRotation.xAxis());
				double alphaZ = cartesian.z * Math.cos(axisRotation.xAxis()) - cartesian.y * Math.sin(axisRotation.xAxis());
				
				//Rotates around Z
				double betaX = alphaX * Math.cos(axisRotation.zAxis()) - alphaY * Math.sin(axisRotation.zAxis());
				double betaY = - alphaX * Math.sin(axisRotation.zAxis()) - alphaY * Math.cos(axisRotation.zAxis());
				double betaZ = alphaZ;
				
				//Rotates around Y
				double gammaX = - betaX * Math.sin(axisRotation.yAxis()) - betaZ * Math.cos(axisRotation.yAxis());
				double gammaY = betaY;
				double gammaZ = betaX * Math.cos(axisRotation.yAxis()) - betaZ * Math.sin(axisRotation.yAxis());

				starInfo.newStar(bufferBuilder, randomsource, relativeCoords, gammaX, gammaY, gammaZ, i);
			}
			return bufferBuilder.end();
		}
		
		protected RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			
			for(int i = 0; i < stars; i++)
			{
				starInfo.createStar(bufferBuilder, randomsource, relativeCoords, i);
			}
			return bufferBuilder.end();
		}
	}*/
}
