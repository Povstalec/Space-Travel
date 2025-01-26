package net.povstalec.spacetravel.common.space.generation.parameters;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.space.generation.ParameterLocations;
import net.povstalec.spacetravel.common.space.generation.SpaceTravelParameters;
import net.povstalec.spacetravel.common.space.generation.ParameterLocation;
import net.povstalec.spacetravel.common.space.space_objects.STStar;
import net.povstalec.stellarview.api.common.space_objects.OrbitingObject;
import net.povstalec.stellarview.api.common.space_objects.StarLike;
import net.povstalec.stellarview.api.common.space_objects.TexturedObject;
import net.povstalec.stellarview.common.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class StarParameters extends OrbitingObjectParameters<STStar>
{
	public static final ResourceLocation STAR_PARAMETERS_LOCATION = new ResourceLocation(SpaceTravel.MODID, "parameters/star");
	public static final ResourceKey<Registry<StarParameters>> REGISTRY_KEY = ResourceKey.createRegistryKey(STAR_PARAMETERS_LOCATION);
	
	protected ArrayList<TextureLayer> textureLayers;
	
	protected SpaceTravelParameters.DoubleRange minSizeRange;
	protected SpaceTravelParameters.DoubleRange maxAlphaRange;
	protected SpaceTravelParameters.DoubleRange minAlphaRange;
	
	
	
	public static final Codec<StarParameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TextureLayer.CODEC.listOf().fieldOf(TexturedObject.TEXTURE_LAYERS).forGetter(parameters -> parameters.textureLayers),
			
			//TODO TexturedObject.FadeOutHandler.CODEC.optionalFieldOf(TexturedObject.FADE_OUT_HANDLER, TexturedObject.FadeOutHandler.DEFAULT_STAR_HANDLER).forGetter(parameters -> parameters.fadeOutHandler),
			
			SpaceTravelParameters.DoubleRange.FULL_RANGE_CODEC.fieldOf(StarLike.MIN_STAR_SIZE).forGetter(parameters -> parameters.minSizeRange),
			SpaceTravelParameters.DoubleRange.FULL_RANGE_CODEC.fieldOf(StarLike.MAX_STAR_ALPHA).forGetter(parameters -> parameters.maxAlphaRange),
			SpaceTravelParameters.DoubleRange.FULL_RANGE_CODEC.fieldOf(StarLike.MIN_STAR_ALPHA).forGetter(parameters -> parameters.minAlphaRange),
			
			//TODO Star.SupernovaInfo.CODEC.optionalFieldOf(Star.SUPERNOVA_INFO).forGetter(star -> Optional.ofNullable(star.supernovaInfo())),
			
			//TODO MassObject.Mass.CODEC.optionalFieldOf(MassObject.MASS).forGetter(star -> Optional.ofNullable(star.mass))
			
			OrbitParameters.CODEC.optionalFieldOf(ORBIT_PARAMETERS).forGetter(parameters -> Optional.ofNullable(parameters.orbitParameters)),
			
			ParameterLocations.CODEC.listOf().optionalFieldOf(INSTANT_CHILDREN).forGetter(parameters -> Optional.ofNullable(parameters.instantChildrenParameters)),
			ParameterLocations.CODEC.listOf().optionalFieldOf(CHILDREN).forGetter(parameters -> Optional.ofNullable(parameters.childrenParameters))
	).apply(instance, StarParameters::new));
	
	public StarParameters(List<TextureLayer> textureLayers, SpaceTravelParameters.DoubleRange minSizeRange,SpaceTravelParameters.DoubleRange maxAlphaRange, SpaceTravelParameters.DoubleRange minAlphaRange,
						  Optional<OrbitParameters> orbitParameters,
						  Optional<List<ParameterLocations>> instantChildrenParameters, Optional<List<ParameterLocations>> childrenParameters)
	{
		super(orbitParameters, instantChildrenParameters, childrenParameters);
		
		this.textureLayers = new ArrayList<>(textureLayers);
		
		this.minSizeRange = minSizeRange;
		this.maxAlphaRange = maxAlphaRange;
		this.minAlphaRange = minAlphaRange;
	}
	
	public STStar generateOrbiting(Random random, long seed, SpaceCoords spaceCoords, AxisRotation axisRotation, @Nullable OrbitingObject.OrbitInfo orbitInfo)
	{
		STStar star = new STStar(Optional.empty(), Either.left(spaceCoords), axisRotation, Optional.ofNullable(orbitInfo),
				textureLayers, TexturedObject.FadeOutHandler.DEFAULT_STAR_HANDLER, (float) minSizeRange.nextDouble(random), (float) maxAlphaRange.nextDouble(random), (float) minAlphaRange.nextDouble(random), Optional.empty(),
				Optional.empty());
		
		return star;
	}
}
