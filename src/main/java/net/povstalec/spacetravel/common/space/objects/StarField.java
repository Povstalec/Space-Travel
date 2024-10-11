package net.povstalec.spacetravel.common.space.objects;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.space.SpaceRegion;
import net.povstalec.spacetravel.common.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class StarField extends SpaceObject
{
	public static final ResourceLocation STAR_FIELD_LOCATION = new ResourceLocation(SpaceTravel.MODID, "star_field");
	public static final ResourceKey<Registry<StarField>> REGISTRY_KEY = ResourceKey.createRegistryKey(STAR_FIELD_LOCATION);
	
	public static final String SEED = "seed";
	public static final String DIAMETER_LY = "diameter_ly";
	public static final String STARS = "stars";
	public static final String TOTAL_STARS = "total_stars";
	public static final String STAR_INFO = "star_info";
	public static final String SPIRAL_ARMS = "spiral_arms";
	public static final String CLUMP_STARS_IN_CENTER = "clump_stars_in_center";
	public static final String X_STRETCH = "x_stretch";
	public static final String Y_STRETCH = "y_stretch";
	public static final String Z_STRETCH = "z_stretch";
	protected StarInfo starInfo;
	
	protected long seed;
	protected boolean clumpStarsInCenter;
	
	protected int diameter;
	protected int stars;
	
	private double xStretch;
	private double yStretch;
	private double zStretch;
	
	protected ArrayList<SpiralArm> spiralArms;
	
	protected int totalStars;
	
	public static final Codec<StarField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(StarField::getParentLocation),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getSpaceCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(StarField::getAxisRotation),
			
			SpaceObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", SpaceObject.FadeOutHandler.DEFAULT_STAR_FIELD_HANDLER).forGetter(StarField::getFadeOutHandler),
			
			StarInfo.CODEC.optionalFieldOf("star_info", StarInfo.DEFAULT_STAR_INFO).forGetter(StarField::getStarInfo),
			Codec.LONG.fieldOf(SEED).forGetter(StarField::getSeed),
			Codec.INT.fieldOf(DIAMETER_LY).forGetter(StarField::getDiameter),
			
			Codec.intRange(1, 30000).fieldOf(STARS).forGetter(StarField::getStars),
			Codec.BOOL.optionalFieldOf("clump_stars_in_center", true).forGetter(StarField::clumpStarsInCenter),
			
			Codec.DOUBLE.optionalFieldOf("x_stretch", 1.0).forGetter(StarField::xStretch),
			Codec.DOUBLE.optionalFieldOf("y_stretch", 1.0).forGetter(StarField::yStretch),
			Codec.DOUBLE.optionalFieldOf("z_stretch", 1.0).forGetter(StarField::zStretch),
			
			SpiralArm.CODEC.listOf().optionalFieldOf("spiral_arms", new ArrayList<SpiralArm>()).forGetter(starField -> starField.spiralArms)
	).apply(instance, StarField::new));
	
	public StarField() {}
	
	public StarField(ResourceLocation objectType, Optional<ResourceLocation> parentLocation, Either<SpaceCoords, StellarCoordinates.Equatorial> coords,
					 AxisRotation axisRotation, FadeOutHandler fadeOutHandler, StarInfo starInfo,
					 long seed, int diameter, int numberOfStars, boolean clumpStarsInCenter,
					 double xStretch, double yStretch, double zStretch, List<SpiralArm> spiralArms)
	{
		super(objectType, parentLocation, coords, axisRotation, fadeOutHandler);
		
		this.starInfo = starInfo;
		this.seed = seed;
		this.diameter = diameter;
		
		this.stars = numberOfStars;
		this.clumpStarsInCenter = clumpStarsInCenter;
		
		this.xStretch = xStretch;
		this.yStretch = yStretch;
		this.zStretch = zStretch;
		
		this.spiralArms = new ArrayList<SpiralArm>(spiralArms);
		
		// Calculate the total amount of stars
		int totalStars = stars;
		for(SpiralArm arm : this.spiralArms)
		{
			totalStars += arm.armStars();
		}
		
		this.totalStars = totalStars;
	}
	
	public StarField(Optional<ResourceLocation> parentLocation, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation, FadeOutHandler fadeOutHandler,
					 StarInfo starInfo, long seed, int diameter, int numberOfStars, boolean clumpStarsInCenter,
					 double xStretch, double yStretch, double zStretch, List<SpiralArm> spiralArms)
	{
		this(STAR_FIELD_LOCATION, parentLocation, coords, axisRotation, fadeOutHandler, starInfo, seed, diameter, numberOfStars, clumpStarsInCenter, xStretch, yStretch, zStretch, spiralArms);
	}
	
	public StarField(Optional<ResourceLocation> parentLocation, SpaceCoords coords, AxisRotation axisRotation, FadeOutHandler fadeOutHandler,
					 StarInfo starInfo, long seed, int diameter, int numberOfStars, boolean clumpStarsInCenter,
					 double xStretch, double yStretch, double zStretch, List<SpiralArm> spiralArms)
	{
		this(parentLocation, Either.left(coords), axisRotation, fadeOutHandler, starInfo, seed, diameter, numberOfStars, clumpStarsInCenter, xStretch, yStretch, zStretch, spiralArms);
	}
	
	public StarField(Optional<ResourceLocation> parentLocation, StellarCoordinates.Equatorial coords, AxisRotation axisRotation, FadeOutHandler fadeOutHandler,
					 StarInfo starInfo, long seed, int diameter, int numberOfStars, boolean clumpStarsInCenter,
					 double xStretch, double yStretch, double zStretch, List<SpiralArm> spiralArms)
	{
		this(parentLocation, Either.right(coords), axisRotation, fadeOutHandler, starInfo, seed, diameter, numberOfStars, clumpStarsInCenter, xStretch, yStretch, zStretch, spiralArms);
	}
	
	public StarInfo getStarInfo()
	{
		return starInfo;
	}
	
	public long getSeed()
	{
		return seed;
	}
	
	public int getDiameter()
	{
		return diameter;
	}
	
	public int getStars()
	{
		return stars;
	}
	public int getTotalStars()
	{
		return totalStars;
	}
	
	public boolean clumpStarsInCenter()
	{
		return clumpStarsInCenter;
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
	
	public List<SpiralArm> getSpiralArms()
	{
		return spiralArms;
	}
	
	public static StarField randomStarField(RandomSource randomsource, long seed, long xPos, long yPos, long zPos)
	{
		long randomX = randomsource.nextLong() % SpaceRegion.LY_PER_REGION - SpaceRegion.LY_PER_REGION_HALF;
		long randomY = randomsource.nextLong() % SpaceRegion.LY_PER_REGION - SpaceRegion.LY_PER_REGION_HALF;
		long randomZ = randomsource.nextLong() % SpaceRegion.LY_PER_REGION - SpaceRegion.LY_PER_REGION_HALF;
		
		long x = randomX + xPos;
		long y = randomY + yPos;
		long z = randomZ + zPos;
		
		double xRot = randomsource.nextDouble() * 360;
		double yRot = randomsource.nextDouble() * 360;
		double zRot = randomsource.nextDouble() * 360;
		
		AxisRotation axisRotation = new AxisRotation(true, xRot, yRot, zRot);
		
		double xStretch = 0.25D + randomsource.nextDouble() * 0.75F;
		double yStretch = 0.25D + randomsource.nextDouble() * 0.75F;
		double zStretch = 0.25D + randomsource.nextDouble() * 0.75F;
		
		int numberOfArms = Math.abs(randomsource.nextInt()) % 13 - 6;
		
		if(numberOfArms < 0)
			numberOfArms = 0;
		
		int stars = Math.abs(randomsource.nextInt()) % 19600 + 400; // 1500
		if(numberOfArms > 0)
			stars = stars / numberOfArms;
		
		int diameter = stars * 60; // 90000
		
		double degrees = 360D / numberOfArms;
		
		ArrayList<StarField.SpiralArm> arms = new ArrayList<StarField.SpiralArm>();
		for(int i = 0; i < numberOfArms; i++)
		{
			arms.add(SpiralArm.randomSpiralArm(randomsource, stars, degrees * i));
		}
		
		StarField starField = new StarField(Optional.empty(), new SpaceCoords(x, y, z), axisRotation, FadeOutHandler.DEFAULT_STAR_FIELD_HANDLER,
				StarInfo.DEFAULT_STAR_INFO, seed, diameter, stars, true, xStretch, yStretch, zStretch, arms);
		
		return starField;
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = super.serializeNBT();
		
		tag.putLong(SEED, seed);
		
		tag.putInt(DIAMETER_LY, diameter);
		tag.putInt(STARS, stars);
		tag.putInt(TOTAL_STARS, totalStars);
		
		tag.putBoolean(CLUMP_STARS_IN_CENTER, clumpStarsInCenter);
		
		tag.putDouble(X_STRETCH, xStretch);
		tag.putDouble(Y_STRETCH, yStretch);
		tag.putDouble(Z_STRETCH, zStretch);
		
		CompoundTag armsTag = new CompoundTag();
		for(int i = 0; i < spiralArms.size(); i++)
		{
			armsTag.put("spiral_arm_" + i, spiralArms.get(i).serializeNBT());
		}
		
		tag.put(SPIRAL_ARMS, armsTag);
		
		tag.put(STAR_INFO, starInfo.serializeNBT());
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		super.deserializeNBT(tag);
		
		seed = tag.getLong(SEED);
		
		diameter = tag.getInt(DIAMETER_LY);
		stars = tag.getInt(STARS);
		totalStars = tag.getInt(TOTAL_STARS);
		
		clumpStarsInCenter = tag.getBoolean(CLUMP_STARS_IN_CENTER);
		
		xStretch = tag.getDouble(X_STRETCH);
		yStretch = tag.getDouble(Y_STRETCH);
		zStretch = tag.getDouble(Z_STRETCH);
		
		this.spiralArms = new ArrayList<SpiralArm>();
		CompoundTag armsTag = tag.getCompound(SPIRAL_ARMS);
		for(String key : armsTag.getAllKeys())
		{
			SpiralArm arm = new SpiralArm();
			arm.deserializeNBT(armsTag.getCompound(key));
			spiralArms.add(arm);
		}
		
		this.starInfo = new StarInfo();
		this.starInfo.deserializeNBT(tag.getCompound(STAR_INFO));
	}
	
	public static class SpiralArm implements INBTSerializable<CompoundTag>
	{
		public static final String STARS = "stars";
		public static final String ARM_ROTATION = "arm_rotation";
		public static final String ARM_LENGTH = "arm_length";
		public static final String ARM_THICKNESS = "arm_thickness";
		public static final String CLUMP_STARS_IN_CENTER = "clump_stars_in_center";
		
		protected int armStars;
		protected double armRotation;
		protected double armLength;
		protected double armThickness;
		protected boolean clumpStarsInCenter;
		
		public static final Codec<SpiralArm> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf(STARS).forGetter(SpiralArm::armStars),
				Codec.DOUBLE.fieldOf(ARM_ROTATION).forGetter(SpiralArm::armRotation),
				Codec.DOUBLE.fieldOf(ARM_LENGTH).forGetter(SpiralArm::armLength),
				Codec.DOUBLE.fieldOf(ARM_THICKNESS).forGetter(SpiralArm::armThickness),
				Codec.BOOL.optionalFieldOf(CLUMP_STARS_IN_CENTER, true).forGetter(SpiralArm::clumpStarsInCenter)
		).apply(instance, SpiralArm::new));
		
		public SpiralArm() {}
		
		public SpiralArm(int armStars, boolean inDegrees, double armRotation, double armLength, double armThickness, boolean clumpStarsInCenter)
		{
			this.armStars = armStars;
			this.armRotation = inDegrees ? Math.toRadians(armRotation) : armRotation;
			this.armLength = armLength;
			this.armThickness = armThickness;
			
			this.clumpStarsInCenter = clumpStarsInCenter;
		}
		
		public SpiralArm(int armStars, double armRotation, double armLength, double armThickness, boolean clumpStarsInCenter)
		{
			this(armStars, true, armRotation, armLength, armThickness, clumpStarsInCenter);
		}
		
		public int armStars()
		{
			return armStars;
		}
		
		public double armRotation()
		{
			return armRotation;
		}
		
		public double armLength()
		{
			return armLength;
		}
		
		public double armThickness()
		{
			return armThickness;
		}
		
		public boolean clumpStarsInCenter()
		{
			return clumpStarsInCenter;
		}
		
		public static SpiralArm randomSpiralArm(RandomSource randomsource, int starFieldStars, double armRotation)
		{
			int stars = starFieldStars + Math.abs(randomsource.nextInt()) % (starFieldStars / 2);
			double armLength = 1.5D + randomsource.nextDouble();
			double armThickness = 1.75D + randomsource.nextDouble();
			boolean clumpStarsInCenter = true;
			
			SpiralArm arm = new SpiralArm(stars, armRotation, armLength, armThickness, clumpStarsInCenter);
			
			return arm;
		}
		
		//============================================================================================
		//*************************************Saving and Loading*************************************
		//============================================================================================
		
		@Override
		public CompoundTag serializeNBT()
		{
			CompoundTag tag = new CompoundTag();
			
			tag.putInt(STARS, armStars);
			
			tag.putDouble(ARM_ROTATION, armRotation);
			tag.putDouble(ARM_LENGTH, armLength);
			tag.putDouble(ARM_THICKNESS, armThickness);
			
			tag.putBoolean(CLUMP_STARS_IN_CENTER, clumpStarsInCenter);
			
			return tag;
		}
		
		@Override
		public void deserializeNBT(CompoundTag tag)
		{
			armStars = tag.getInt(STARS);
			
			armRotation = tag.getDouble(ARM_ROTATION);
			armLength = tag.getDouble(ARM_LENGTH);
			armThickness = tag.getDouble(ARM_THICKNESS);
			
			clumpStarsInCenter = tag.getBoolean(CLUMP_STARS_IN_CENTER);
		}
	}
}
