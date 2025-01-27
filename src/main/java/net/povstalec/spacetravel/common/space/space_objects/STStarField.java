package net.povstalec.spacetravel.common.space.space_objects;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.common.space.GenerationObject;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.StarField;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class STStarField extends StarField implements GenerationObject
{
	@Nullable
	protected ResourceLocation generationParameters;
	
	public static final Codec<STStarField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(STStarField::getParentLocation),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(STStarField::getAxisRotation),
			
			Codec.intRange(0, 4000).optionalFieldOf("dust_clouds", 0).forGetter(STStarField::getDustClouds),
			ResourceLocation.CODEC.optionalFieldOf("dust_cloud_info").forGetter(starField -> Optional.ofNullable(starField.dustCloudInfo)),
			ResourceLocation.CODEC.optionalFieldOf("dust_cloud_texture", DEFAULT_DUST_CLOUD_TEXTURE).forGetter(STStarField::getDustCloudTexture),
			
			ResourceLocation.CODEC.optionalFieldOf("star_info").forGetter(starField -> Optional.ofNullable(starField.starInfo)),
			ResourceLocation.CODEC.optionalFieldOf("star_texture", DEFAULT_STAR_TEXTURE).forGetter(starField -> starField.starTexture),
			Codec.LONG.fieldOf("seed").forGetter(STStarField::getSeed),
			Codec.INT.fieldOf("diameter_ly").forGetter(STStarField::getDiameter),
			
			Codec.intRange(0, 30000).fieldOf("stars").forGetter(STStarField::getStars),
			Codec.BOOL.optionalFieldOf("clump_stars_in_center", true).forGetter(STStarField::clumpStarsInCenter),
			
			Codec.DOUBLE.optionalFieldOf("x_stretch", 1.0).forGetter(STStarField::xStretch),
			Codec.DOUBLE.optionalFieldOf("y_stretch", 1.0).forGetter(STStarField::yStretch),
			Codec.DOUBLE.optionalFieldOf("z_stretch", 1.0).forGetter(STStarField::zStretch),
			
			SpiralArm.CODEC.listOf().optionalFieldOf("spiral_arms", new ArrayList<SpiralArm>()).forGetter(starField -> starField.spiralArms)
	).apply(instance, STStarField::new));
	
	public STStarField() {}
	
	public STStarField(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
					   int dustClouds, Optional<ResourceLocation> dustCloudInfo, ResourceLocation dustCloudTexture,
					   Optional<ResourceLocation> starInfo, ResourceLocation starTexture,
					   long seed, int diameter, int numberOfStars, boolean clumpStarsInCenter,
					   double xStretch, double yStretch, double zStretch, List<SpiralArm> spiralArms)
	{
		super(parent, coords, axisRotation, dustClouds, dustCloudInfo, dustCloudTexture, starInfo, starTexture, seed, diameter, numberOfStars, clumpStarsInCenter, xStretch, yStretch, zStretch, spiralArms);
	}
	
	//============================================================================================
	//*****************************************Geeration******************************************
	//============================================================================================
	
	@Override
	@Nullable
	public ResourceLocation getGenerationParameters()
	{
		return generationParameters;
	}
	
	@Override
	public void setGenerationParameters(ResourceLocation generationParameters)
	{
		this.generationParameters = generationParameters;
	}
	
	@Override
	public long generationSeed()
	{
		return this.getSeed();
	}
	
	@Override
	public SpaceObject generationParent()
	{
		return this;
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = super.serializeNBT();
		
		if(generationParameters != null)
			tag.putString(GENERATION_PARAMETERS, generationParameters.toString());
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		super.deserializeNBT(tag);
		
		if(tag.contains(GENERATION_PARAMETERS))
			this.generationParameters = new ResourceLocation(tag.getString(GENERATION_PARAMETERS));
	}
}
