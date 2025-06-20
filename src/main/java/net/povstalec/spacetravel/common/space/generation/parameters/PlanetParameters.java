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
import net.povstalec.spacetravel.common.space.space_objects.STPlanet;
import net.povstalec.spacetravel.common.space.space_objects.STStar;
import net.povstalec.stellarview.api.common.space_objects.OrbitingObject;
import net.povstalec.stellarview.api.common.space_objects.StarLike;
import net.povstalec.stellarview.api.common.space_objects.TexturedObject;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.TextureLayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class PlanetParameters extends OrbitingObjectParameters<STPlanet>
{
	public static final String WORLDGEN_INFO = "worldgen_info";
	
	public static final ResourceLocation PLANET_PARAMETERS_LOCATION = new ResourceLocation(SpaceTravel.MODID, "parameters/planet");
	public static final ResourceKey<Registry<PlanetParameters>> REGISTRY_KEY = ResourceKey.createRegistryKey(PLANET_PARAMETERS_LOCATION);
	
	protected ArrayList<TextureLayer> textureLayers;
	
	@Nullable
	protected WorldGenInfo worldGenInfo;
	
	public static final Codec<PlanetParameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TextureLayer.CODEC.listOf().fieldOf(TexturedObject.TEXTURE_LAYERS).forGetter(parameters -> parameters.textureLayers),
			
			//TODO TexturedObject.FadeOutHandler.CODEC.optionalFieldOf(TexturedObject.FADE_OUT_HANDLER, TexturedObject.FadeOutHandler.DEFAULT_STAR_HANDLER).forGetter(parameters -> parameters.fadeOutHandler),
			
			//TODO MassObject.Mass.CODEC.optionalFieldOf(MassObject.MASS).forGetter(star -> Optional.ofNullable(star.mass)),
			
			OrbitParameters.CODEC.optionalFieldOf(ORBIT_PARAMETERS).forGetter(parameters -> Optional.ofNullable(parameters.orbitParameters)),
			
			WorldGenInfo.CODEC.optionalFieldOf(WORLDGEN_INFO).forGetter(planet -> Optional.ofNullable(planet.worldGenInfo)),
			
			ParameterLocations.CODEC.listOf().optionalFieldOf(INSTANT_CHILDREN).forGetter(parameters -> Optional.ofNullable(parameters.instantChildrenParameters)),
			ParameterLocations.CODEC.listOf().optionalFieldOf(CHILDREN).forGetter(parameters -> Optional.ofNullable(parameters.childrenParameters))
	).apply(instance, PlanetParameters::new));
	
	public PlanetParameters(List<TextureLayer> textureLayers, Optional<OrbitParameters> orbitParameters,
							Optional<WorldGenInfo> worldGenInfo,
							Optional<List<ParameterLocations>> instantChildrenParameters, Optional<List<ParameterLocations>> childrenParameters)
	{
		super(orbitParameters, instantChildrenParameters, childrenParameters);
		
		this.textureLayers = new ArrayList<>(textureLayers);
	}
	
	public STPlanet generateOrbiting(Random random, long seed, SpaceCoords spaceCoords, AxisRotation axisRotation, @Nullable OrbitingObject.OrbitInfo orbitInfo)
	{
		STPlanet planet = new STPlanet(Optional.empty(), Either.left(spaceCoords), axisRotation, Optional.ofNullable(orbitInfo),
				textureLayers, TexturedObject.FadeOutHandler.DEFAULT_STAR_HANDLER, Optional.empty(),
				Optional.empty(), Optional.ofNullable(worldGenInfo));
		
		return planet;
	}
}
