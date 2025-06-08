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
			ResourceLocation.CODEC.optionalFieldOf(PARENT_LOCATION).forGetter(STStarField::getParentLocation),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf(COORDS).forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf(AXIS_ROTATION).forGetter(STStarField::getAxisRotation),
			
			Codec.intRange(0, 4000).optionalFieldOf(DUST_CLOUDS, 0).forGetter(STStarField::getDustClouds),
			ResourceLocation.CODEC.optionalFieldOf(DUST_CLOUD_INFO).forGetter(starField -> Optional.ofNullable(starField.dustCloudInfo)),
			ResourceLocation.CODEC.optionalFieldOf(DUST_CLOUD_TEXTURE, DEFAULT_DUST_CLOUD_TEXTURE).forGetter(STStarField::getDustCloudTexture),
			Codec.BOOL.optionalFieldOf(CLUMP_DUST_CLOUDS_IN_CENTER, true).forGetter(STStarField::clumpDustCloudsInCenter),
			Stretch.CODEC.optionalFieldOf(DUST_CLOUD_STRETCH, Stretch.DEFAULT_STRETCH).forGetter(STStarField::dustCloudStretch),
			
			Codec.intRange(0, 30000).fieldOf(STARS).forGetter(StarField::getStars),
			ResourceLocation.CODEC.optionalFieldOf(STAR_INFO).forGetter(starField -> Optional.ofNullable(starField.starInfo)),
			ResourceLocation.CODEC.optionalFieldOf(STAR_TEXTURE, DEFAULT_STAR_TEXTURE).forGetter(starField -> starField.starTexture),
			Codec.BOOL.optionalFieldOf(CLUMP_STARS_IN_CENTER, true).forGetter(STStarField::clumpStarsInCenter),
			Stretch.CODEC.optionalFieldOf(STAR_STRETCH, Stretch.DEFAULT_STRETCH).forGetter(STStarField::starStretch),
			
			Codec.LONG.fieldOf(SEED).forGetter(STStarField::getSeed),
			Codec.INT.fieldOf(DIAMETER_LY).forGetter(STStarField::getDiameter),
			SpiralArm.CODEC.listOf().optionalFieldOf(SPIRAL_ARMS, new ArrayList<SpiralArm>()).forGetter(starField -> starField.spiralArms)
	).apply(instance, STStarField::new));
	
	public STStarField() {}
	
	public STStarField(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
					   int dustClouds, Optional<ResourceLocation> dustCloudInfo, ResourceLocation dustCloudTexture, boolean clumpDustCloudsInCenter, Stretch dustCloudStretch,
					   int stars, Optional<ResourceLocation> starInfo, ResourceLocation starTexture, boolean clumpStarsInCenter, Stretch starStretch,
					   long seed, int diameter, List<SpiralArm> spiralArms)
	{
		super(parent, coords, axisRotation, dustClouds, dustCloudInfo, dustCloudTexture, clumpDustCloudsInCenter, dustCloudStretch, stars, starInfo, starTexture, clumpStarsInCenter, starStretch, seed, diameter, spiralArms);
	}
	
	//============================================================================================
	//*****************************************Generation*****************************************
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
