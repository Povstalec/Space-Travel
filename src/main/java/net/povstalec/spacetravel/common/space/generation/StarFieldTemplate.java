package net.povstalec.spacetravel.common.space.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;
import net.povstalec.spacetravel.common.space.objects.StarField;
import net.povstalec.spacetravel.common.util.*;

import java.util.*;

public class StarFieldTemplate
{
	public static final ResourceLocation STAR_FIELD_TEMPLATE_LOCATION = new ResourceLocation(SpaceTravel.MODID, "template/star_field");
	public static final ResourceKey<Registry<StarFieldTemplate>> REGISTRY_KEY = ResourceKey.createRegistryKey(STAR_FIELD_TEMPLATE_LOCATION);
	
	public static final SpiralArmTemplate DEFAULT_SPIRAL_ARM = new SpiralArmTemplate(1, new SpaceTravelParameters.IntRange(80, 100),
			new SpaceTravelParameters.IntRange(600, 1000), true, new SpaceTravelParameters.DoubleRange(0, 0),
			new SpaceTravelParameters.DoubleRange(1.5, 2.0), new SpaceTravelParameters.DoubleRange(2.0, 2.5));
	
	public static final StarFieldTemplate DEFAULT_STAR_FIELD_TEMPLATE = new StarFieldTemplate(
			1, StarField.DEFAULT_DUST_CLOUD_TEXTURE, new SpaceTravelParameters.IntRange(80, 100),
			new SpaceTravelParameters.IntRange(600, 1000), StarInfo.DEFAULT_STAR_INFO, true,
			new SpaceTravelParameters.IntRange(12000, 120000), new SpaceTravelParameters.DoubleRange(0.25, 1.0),
			new SpaceTravelParameters.DoubleRange(0.25, 1.0), new SpaceTravelParameters.DoubleRange(0.25, 1.0),
			new SpaceTravelParameters.IntRange(2, 4), Arrays.asList(DEFAULT_SPIRAL_ARM));
	
	protected int weight; // TODO Is weight necessary?
	protected Optional<ArrayList<ResourceLocation>> universes;
	
	protected ResourceLocation dustCloudTexture;
	//TODO dustCloudInfo
	protected SpaceTravelParameters.IntRange dustCloudsRange;
	
	protected SpaceTravelParameters.IntRange starsRange;
	protected StarInfo starInfo;
	protected boolean clumpStarsInCenter;
	//TODO starInfo;
	
	protected SpaceTravelParameters.IntRange diameterRange;
	protected SpaceTravelParameters.DoubleRange xStretchRange;
	protected SpaceTravelParameters.DoubleRange yStretchRange;
	protected SpaceTravelParameters.DoubleRange zStretchRange;
	
	//TODO spiralArms
	protected SpaceTravelParameters.IntRange numberOfArmsRange;
	protected ArrayList<SpiralArmTemplate> spiralArmTemplates;
	protected int totalArmWeight;
	
	public static final Codec<StarFieldTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight").forGetter(template -> template.weight),
			
			ResourceLocation.CODEC.optionalFieldOf("dust_cloud_texture", StarField.DEFAULT_DUST_CLOUD_TEXTURE).forGetter(template -> template.dustCloudTexture),
			SpaceTravelParameters.DUST_CLOUD_RANGE_CODEC.fieldOf("dust_clouds").forGetter(template -> template.dustCloudsRange),
			
			SpaceTravelParameters.STAR_RANGE_CODEC.fieldOf("stars").forGetter(template -> template.starsRange),
			StarInfo.CODEC.optionalFieldOf("star_info", StarInfo.DEFAULT_STAR_INFO).forGetter(template -> template.starInfo),
			Codec.BOOL.optionalFieldOf("clump_stars_in_center", true).forGetter(template -> template.clumpStarsInCenter),
			
			SpaceTravelParameters.DIAMETER_RANGE_CODEC.fieldOf("diameter").forGetter(template -> template.diameterRange),
			SpaceTravelParameters.DOUBLE_RANGE_CODEC.fieldOf("x_stretch").forGetter(template -> template.xStretchRange),
			SpaceTravelParameters.DOUBLE_RANGE_CODEC.fieldOf("y_stretch").forGetter(template -> template.yStretchRange),
			SpaceTravelParameters.DOUBLE_RANGE_CODEC.fieldOf("z_stretch").forGetter(template -> template.zStretchRange),
			
			SpaceTravelParameters.ARM_NUMBER_RANGE_CODEC.optionalFieldOf("number_of_arms", new SpaceTravelParameters.IntRange(0, 0)).forGetter(template -> template.numberOfArmsRange),
			SpiralArmTemplate.CODEC.listOf().optionalFieldOf("spiral_arms", new ArrayList<>()).forGetter(template -> template.spiralArmTemplates)
	).apply(instance, StarFieldTemplate::new));
	
	public StarFieldTemplate(int weight, ResourceLocation dustCloudTexture, SpaceTravelParameters.IntRange dustCloudsRange, SpaceTravelParameters.IntRange starsRange, StarInfo starInfo, boolean clumpStarsInCenter,
							 SpaceTravelParameters.IntRange diameterRange, SpaceTravelParameters.DoubleRange xStretchRange, SpaceTravelParameters.DoubleRange yStretchRange, SpaceTravelParameters.DoubleRange zStretchRange,
							 SpaceTravelParameters.IntRange numberOfArmsRange, List<SpiralArmTemplate> spiralArmTemplates)
	{
		this.weight = weight;
		
		this.dustCloudTexture = dustCloudTexture;
		//TODO dustCloudInfo
		this.dustCloudsRange = dustCloudsRange;
		
		this.starsRange = starsRange;
		this.starInfo = starInfo;
		this.clumpStarsInCenter = clumpStarsInCenter;
		
		this.diameterRange = diameterRange;
		this.xStretchRange = xStretchRange;
		this.yStretchRange = yStretchRange;
		this.zStretchRange = zStretchRange;
		
		this.numberOfArmsRange = numberOfArmsRange;
		
		this.spiralArmTemplates = new ArrayList<>(spiralArmTemplates);
		int totalArmWeight = 0;
		for(SpiralArmTemplate armTemplate : spiralArmTemplates)
		{
			totalArmWeight += armTemplate.weight;
		}
		
		this.totalArmWeight = weight;
	}
	
	public int getWeight()
	{
		return weight;
	}
	
	//TODO Should this also decide Space Coords and Axis Rotation instead of them being completely random?
	public StarField generateStarField(Random random, long seed, SpaceCoords spaceCoords, AxisRotation axisRotation)
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
		
		return new StarField(Optional.empty(), spaceCoords, axisRotation, SpaceObject.FadeOutHandler.DEFAULT_STAR_FIELD_HANDLER,
				dustClouds, DustCloudInfo.randomDustCloudInfo(random), dustCloudTexture,
				starInfo, seed, diameter, stars, clumpStarsInCenter, xStretch, yStretch, zStretch, arms);
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
	
	
	
	public static class SpiralArmTemplate
	{
		protected int weight;
		
		//TODO dustCloudInfo
		protected SpaceTravelParameters.IntRange dustCloudsRange;
		
		protected SpaceTravelParameters.IntRange starsRange;
		protected boolean clumpStarsInCenter;
		
		protected SpaceTravelParameters.DoubleRange armRotationOffsetRange;
		protected SpaceTravelParameters.DoubleRange armLengthRange;
		protected SpaceTravelParameters.DoubleRange armThicknessRange;
		
		public static final Codec<SpiralArmTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight").forGetter(template -> template.weight),
				
				SpaceTravelParameters.DUST_CLOUD_RANGE_CODEC.fieldOf("dust_clouds").forGetter(template -> template.dustCloudsRange),
				
				SpaceTravelParameters.STAR_RANGE_CODEC.fieldOf("stars").forGetter(template -> template.starsRange),
				Codec.BOOL.optionalFieldOf("clump_stars_in_center", true).forGetter(template -> template.clumpStarsInCenter),
				
				SpaceTravelParameters.ANGLE_RANGE_CODEC.fieldOf("arm_rotation_offset").forGetter(template -> template.armRotationOffsetRange),
				SpaceTravelParameters.DOUBLE_POSITIVE_RANGE_CODEC.fieldOf("arm_length").forGetter(template -> template.armLengthRange),
				SpaceTravelParameters.DOUBLE_POSITIVE_RANGE_CODEC.fieldOf("arm_thickness").forGetter(template -> template.armThicknessRange)
		).apply(instance, SpiralArmTemplate::new));
		
		public SpiralArmTemplate(int weight, SpaceTravelParameters.IntRange dustCloudsRange, SpaceTravelParameters.IntRange starsRange, boolean clumpStarsInCenter,
								 SpaceTravelParameters.DoubleRange armRotationOffsetRange, SpaceTravelParameters.DoubleRange armLengthRange, SpaceTravelParameters.DoubleRange armThicknessRange)
		{
			this.weight = weight;
			
			//TODO dustCloudInfo
			this.dustCloudsRange = dustCloudsRange;
			
			this.starsRange = starsRange;
			this.clumpStarsInCenter = clumpStarsInCenter;
			
			this.armRotationOffsetRange = armRotationOffsetRange;
			this.armLengthRange = armLengthRange;
			this.armThicknessRange = armThicknessRange;
		}
		
		public StarField.SpiralArm generateSpiralArm(Random random, double armRotation)
		{
			int stars = starsRange.nextInt(random);
			
			double armRotationOffset = Math.toRadians(armRotationOffsetRange.nextDouble(random));
			double armLength = armLengthRange.nextDouble(random);
			double armThickness = armThicknessRange.nextDouble(random);
			
			int dustClouds = dustCloudsRange.nextInt(random);
			//TODO Randomizing (Also randomize dust cloud info)
			DustCloudInfo dustCloudInfo = null;
			
			return new StarField.SpiralArm(dustClouds, Optional.ofNullable(dustCloudInfo), stars, armRotation + armRotationOffset, armLength, armThickness, clumpStarsInCenter);
		}
	}
}
