package net.povstalec.spacetravel.common.space.generation.templates;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.space.generation.*;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.StarField;
import net.povstalec.stellarview.common.util.*;

import java.util.*;

public class StarFieldParameters extends SpaceObjectParameters<StarField>
{
	public static final ResourceLocation MAIN_SEQUENCE_DISTRIBUTION = new ResourceLocation(SpaceTravel.MODID, "main_sequence_distribution");
	public static final ResourceLocation WHITE_DUST_CLOUDS = new ResourceLocation(SpaceTravel.MODID, "white_dust_clouds");
	
	public static final ResourceLocation STAR_FIELD_PARAMETERS_LOCATION = new ResourceLocation(SpaceTravel.MODID, "parameters/star_field");
	public static final ResourceKey<Registry<StarFieldParameters>> REGISTRY_KEY = ResourceKey.createRegistryKey(STAR_FIELD_PARAMETERS_LOCATION);
	
	public static final SpiralArmTemplate DEFAULT_SPIRAL_ARM = new SpiralArmTemplate(1, new SpaceTravelParameters.IntRange(80, 100), new ArrayList<WeightedDustCloudInfo>(),
			new SpaceTravelParameters.IntRange(600, 1000), true, new SpaceTravelParameters.DoubleRange(0, 0),
			new SpaceTravelParameters.DoubleRange(1.5, 2.0), new SpaceTravelParameters.DoubleRange(2.0, 2.5));
	
	public static final StarFieldParameters DEFAULT_STAR_FIELD_PARAMETERS = new StarFieldParameters(
			StarField.DEFAULT_DUST_CLOUD_TEXTURE, new SpaceTravelParameters.IntRange(80, 100), new ArrayList<WeightedDustCloudInfo>(),
			StarField.DEFAULT_STAR_TEXTURE, new SpaceTravelParameters.IntRange(600, 1000), new ArrayList<WeightedStarInfo>(), true,
			new SpaceTravelParameters.IntRange(12000, 120000), new SpaceTravelParameters.DoubleRange(0.25, 1.0),
			new SpaceTravelParameters.DoubleRange(0.25, 1.0), new SpaceTravelParameters.DoubleRange(0.25, 1.0),
			new SpaceTravelParameters.IntRange(2, 4), Arrays.asList(DEFAULT_SPIRAL_ARM), new ArrayList<ParameterLocation>());
	
	protected Optional<ArrayList<ResourceLocation>> universes;
	
	protected ResourceLocation dustCloudTexture;
	protected SpaceTravelParameters.IntRange dustCloudsRange;
	
	protected ArrayList<WeightedDustCloudInfo> dustCloudInfo;
	protected int dustCloudWeight;
	
	protected SpaceTravelParameters.IntRange starsRange;
	protected ResourceLocation starTexture;
	protected boolean clumpStarsInCenter;
	
	protected ArrayList<WeightedStarInfo> starInfo;
	protected int starInfoWeight;
	
	protected SpaceTravelParameters.IntRange diameterRange;
	protected SpaceTravelParameters.DoubleRange xStretchRange;
	protected SpaceTravelParameters.DoubleRange yStretchRange;
	protected SpaceTravelParameters.DoubleRange zStretchRange;
	
	protected SpaceTravelParameters.IntRange numberOfArmsRange;
	protected ArrayList<SpiralArmTemplate> spiralArmTemplates;
	protected int totalArmWeight;
	
	public static final Codec<StarFieldParameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("dust_cloud_texture", StarField.DEFAULT_DUST_CLOUD_TEXTURE).forGetter(parameters -> parameters.dustCloudTexture),
			SpaceTravelParameters.IntRange.DUST_CLOUD_RANGE_CODEC.fieldOf("dust_clouds").forGetter(parameters -> parameters.dustCloudsRange),
			WeightedDustCloudInfo.CODEC.listOf().optionalFieldOf("dust_cloud_info", new ArrayList<WeightedDustCloudInfo>()).forGetter(parameters -> parameters.dustCloudInfo),
			
			ResourceLocation.CODEC.optionalFieldOf("star_texture", StarField.DEFAULT_STAR_TEXTURE).forGetter(parameters -> parameters.starTexture),
			SpaceTravelParameters.IntRange.STAR_RANGE_CODEC.fieldOf("stars").forGetter(template -> template.starsRange),
			WeightedStarInfo.CODEC.listOf().optionalFieldOf("star_info", new ArrayList<WeightedStarInfo>()).forGetter(parameters -> parameters.starInfo),
			Codec.BOOL.optionalFieldOf("clump_stars_in_center", true).forGetter(parameters -> parameters.clumpStarsInCenter),
			
			SpaceTravelParameters.IntRange.DIAMETER_RANGE_CODEC.fieldOf("diameter").forGetter(parameters -> parameters.diameterRange),
			SpaceTravelParameters.DoubleRange.FULL_RANGE_CODEC.fieldOf("x_stretch").forGetter(parameters -> parameters.xStretchRange),
			SpaceTravelParameters.DoubleRange.FULL_RANGE_CODEC.fieldOf("y_stretch").forGetter(parameters -> parameters.yStretchRange),
			SpaceTravelParameters.DoubleRange.FULL_RANGE_CODEC.fieldOf("z_stretch").forGetter(parameters -> parameters.zStretchRange),
			
			SpaceTravelParameters.IntRange.ARM_NUMBER_RANGE_CODEC.optionalFieldOf("number_of_arms", new SpaceTravelParameters.IntRange(0, 0)).forGetter(parameters -> parameters.numberOfArmsRange),
			SpiralArmTemplate.CODEC.listOf().optionalFieldOf("spiral_arms", new ArrayList<>()).forGetter(parameters -> parameters.spiralArmTemplates),
			
			ParameterLocation.CODEC.listOf().optionalFieldOf(CHILDREN, new ArrayList<>()).forGetter(parameters -> parameters.childrenParameters)
	).apply(instance, StarFieldParameters::new));
	
	public StarFieldParameters(ResourceLocation dustCloudTexture, SpaceTravelParameters.IntRange dustCloudsRange, List<WeightedDustCloudInfo> dustCloudInfo,
							   ResourceLocation starTexture, SpaceTravelParameters.IntRange starsRange, List<WeightedStarInfo> starInfo, boolean clumpStarsInCenter,
							   SpaceTravelParameters.IntRange diameterRange, SpaceTravelParameters.DoubleRange xStretchRange, SpaceTravelParameters.DoubleRange yStretchRange, SpaceTravelParameters.DoubleRange zStretchRange,
							   SpaceTravelParameters.IntRange numberOfArmsRange, List<SpiralArmTemplate> spiralArmTemplates, List<ParameterLocation> childrenParameters)
	{
		super(childrenParameters);
		
		this.dustCloudTexture = dustCloudTexture;
		this.dustCloudsRange = dustCloudsRange;
		
		this.starsRange = starsRange;
		
		this.starInfo = new ArrayList(starInfo);
		for(WeightedStarInfo info : this.starInfo)
		{
			this.starInfoWeight += info.weight();
		}
		
		this.dustCloudInfo = new ArrayList(dustCloudInfo);
		for(WeightedDustCloudInfo info : this.dustCloudInfo)
		{
			this.dustCloudWeight += info.weight();
		}
		
		this.starTexture = starTexture;
		this.clumpStarsInCenter = clumpStarsInCenter;
		
		this.diameterRange = diameterRange;
		this.xStretchRange = xStretchRange;
		this.yStretchRange = yStretchRange;
		this.zStretchRange = zStretchRange;
		
		this.numberOfArmsRange = numberOfArmsRange;
		
		this.spiralArmTemplates = new ArrayList<>(spiralArmTemplates);
		this.totalArmWeight = 0;
		for(SpiralArmTemplate armTemplate : spiralArmTemplates)
		{
			totalArmWeight += armTemplate.weight;
		}
	}
	
	public ResourceLocation randomDustCloudInfo(Random random)
	{
		if(dustCloudInfo.isEmpty())
			return WHITE_DUST_CLOUDS;
		
		int i = 0;
		
		for(int weight = random.nextInt(0, dustCloudWeight + 1); i < dustCloudInfo.size() - 1; i++)
		{
			weight -= dustCloudInfo.get(i).weight();
			
			if(weight <= 0)
				break;
		}
		
		return dustCloudInfo.get(i).dustCloudInfo();
	}
	
	public ResourceLocation randomStarInfo(Random random)
	{
		if(starInfo.isEmpty())
			return MAIN_SEQUENCE_DISTRIBUTION;
		
		int i = 0;
		
		for(int weight = random.nextInt(0, starInfoWeight + 1); i < starInfo.size() - 1; i++)
		{
			weight -= starInfo.get(i).weight();
			
			if(weight <= 0)
				break;
		}
		
		return starInfo.get(i).starInfo();
	}
	
	protected SpiralArmTemplate randomArmTemplate(Random random)
	{
		if(spiralArmTemplates.isEmpty())
			return DEFAULT_SPIRAL_ARM;
		
		int i = 0;
		
		for(int weight = random.nextInt(0, totalArmWeight); i < spiralArmTemplates.size() - 1; i++)
		{
			weight -= spiralArmTemplates.get(i).weight;
			
			if(weight <= 0)
				break;
		}
		
		return spiralArmTemplates.get(i);
	}
	
	public StarField generate(Random random, long seed, SpaceCoords spaceCoords, AxisRotation axisRotation)
	{
		int dustClouds = dustCloudsRange.nextInt(random);
		
		int stars = starsRange.nextInt(random);
		
		int diameter = diameterRange.nextInt(random);
		double xStretch = xStretchRange.nextDouble(random);
		double yStretch = yStretchRange.nextDouble(random);
		double zStretch = zStretchRange.nextDouble(random);
		
		int numberOfArms = numberOfArmsRange.nextInt(random);
		double degrees = 360D / numberOfArms;
		ArrayList<StarField.SpiralArm> arms = new ArrayList<StarField.SpiralArm>();
		for(int i = 0; i < numberOfArms; i++)
		{
			arms.add(randomArmTemplate(random).generateSpiralArm(random, degrees * i));
		}
		
		StarField starField = new StarField(Optional.empty(), Either.left(spaceCoords), axisRotation,
				dustClouds, Optional.ofNullable(randomDustCloudInfo(random)), dustCloudTexture,
				Optional.ofNullable(randomStarInfo(random)), starTexture, seed, diameter, stars, clumpStarsInCenter, xStretch, yStretch, zStretch, arms);
		
		Random starFieldRandom = new Random(starField.getSeed());
		for(ParameterLocation parameterLocation : childrenParameters)
		{
			int count = parameterLocation.count().nextInt(starFieldRandom);
			for(int i = 0; i < count; i++)
			{
				//TODO Offset
				
				long randomX = starFieldRandom.nextLong(0, diameter) - (diameter / 2);
				long randomY = starFieldRandom.nextLong(0, diameter) - (diameter / 2);
				long randomZ = starFieldRandom.nextLong(0, diameter) - (diameter / 2);
				
				//randomChildTemplate(starFieldRandom)
				SpaceObjectParameters parameters = SpaceObjectParameterRegistry.get(parameterLocation.location());
				if(parameters != null)
				{
					SpaceObject child = parameters.generate(starFieldRandom, seed, new SpaceCoords(randomX, randomY, randomZ), new AxisRotation());
					
					if(child != null)
						starField.addChild(child);
				}
			}
		}
		
		return starField;
	}
	
	
	
	public static class SpiralArmTemplate
	{
		protected int weight;
		
		protected SpaceTravelParameters.IntRange dustCloudsRange;
		
		protected ArrayList<WeightedDustCloudInfo> dustCloudInfo;
		protected int dustCloudWeight;
		
		protected SpaceTravelParameters.IntRange starsRange;
		protected boolean clumpStarsInCenter;
		
		protected SpaceTravelParameters.DoubleRange armRotationOffsetRange;
		protected SpaceTravelParameters.DoubleRange armLengthRange;
		protected SpaceTravelParameters.DoubleRange armThicknessRange;
		
		public static final Codec<SpiralArmTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight").forGetter(template -> template.weight),
				
				SpaceTravelParameters.IntRange.DUST_CLOUD_RANGE_CODEC.fieldOf("dust_clouds").forGetter(template -> template.dustCloudsRange),
				WeightedDustCloudInfo.CODEC.listOf().optionalFieldOf("dust_cloud_info", new ArrayList<WeightedDustCloudInfo>()).forGetter(template -> template.dustCloudInfo),
				
				SpaceTravelParameters.IntRange.STAR_RANGE_CODEC.fieldOf("stars").forGetter(template -> template.starsRange),
				Codec.BOOL.optionalFieldOf("clump_stars_in_center", true).forGetter(template -> template.clumpStarsInCenter),
				
				SpaceTravelParameters.DoubleRange.ANGLE_RANGE_CODEC.fieldOf("arm_rotation_offset").forGetter(template -> template.armRotationOffsetRange),
				SpaceTravelParameters.DoubleRange.POSITIVE_RANGE_CODEC.fieldOf("arm_length").forGetter(template -> template.armLengthRange),
				SpaceTravelParameters.DoubleRange.POSITIVE_RANGE_CODEC.fieldOf("arm_thickness").forGetter(template -> template.armThicknessRange)
		).apply(instance, SpiralArmTemplate::new));
		
		public SpiralArmTemplate(int weight, SpaceTravelParameters.IntRange dustCloudsRange, List<WeightedDustCloudInfo> dustCloudInfo, SpaceTravelParameters.IntRange starsRange, boolean clumpStarsInCenter,
								 SpaceTravelParameters.DoubleRange armRotationOffsetRange, SpaceTravelParameters.DoubleRange armLengthRange, SpaceTravelParameters.DoubleRange armThicknessRange)
		{
			this.weight = weight;
			
			this.dustCloudsRange = dustCloudsRange;
			
			this.starsRange = starsRange;
			this.clumpStarsInCenter = clumpStarsInCenter;
			
			this.armRotationOffsetRange = armRotationOffsetRange;
			this.armLengthRange = armLengthRange;
			this.armThicknessRange = armThicknessRange;
			
			this.dustCloudInfo = new ArrayList(dustCloudInfo);
			for(WeightedDustCloudInfo info : this.dustCloudInfo)
			{
				this.dustCloudWeight += info.weight();
			}
		}
		
		public int getWeight()
		{
			return weight;
		}
		
		public ResourceLocation randomDustCloudInfo(Random random)
		{
			if(dustCloudInfo.isEmpty())
				return null;
			
			int i = 0;
			
			for(int weight = random.nextInt(0, dustCloudWeight + 1); i < dustCloudInfo.size() - 1; i++)
			{
				weight -= dustCloudInfo.get(i).weight();
				
				if(weight <= 0)
					break;
			}
			
			return dustCloudInfo.get(i).dustCloudInfo();
		}
		
		public StarField.SpiralArm generateSpiralArm(Random random, double armRotation)
		{
			int stars = starsRange.nextInt(random);
			
			double armRotationOffset = Math.toRadians(armRotationOffsetRange.nextDouble(random));
			double armLength = armLengthRange.nextDouble(random);
			double armThickness = armThicknessRange.nextDouble(random);
			
			int dustClouds = dustCloudsRange.nextInt(random);
			
			return new StarField.SpiralArm(dustClouds, Optional.ofNullable(randomDustCloudInfo(random)), stars, armRotation + armRotationOffset, armLength, armThickness, clumpStarsInCenter);
		}
	}
}
