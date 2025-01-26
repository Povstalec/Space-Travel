package net.povstalec.spacetravel.common.space.generation;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.common.space.generation.parameters.OrbitingObjectParameters;

import javax.annotation.Nullable;

public class ParameterLocation
{
	public static final String PARAMETERS = "parameters";
	public static final String WEIGHT = "weight";
	public static final String POSITION = "position";
	
	private ResourceLocation location;
	private int weight;
	
	private Either<SpaceOffsetParameters, OrbitingObjectParameters.OrbitParameters> pos;
	
	public static final Codec<ParameterLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf(PARAMETERS).forGetter(info -> info.location),
			Codec.INT.fieldOf(WEIGHT).forGetter(info -> info.weight),
			
			Codec.either(SpaceOffsetParameters.CODEC, OrbitingObjectParameters.OrbitParameters.CODEC).optionalFieldOf(POSITION, Either.left(new SpaceOffsetParameters())).forGetter(info -> info.pos)
	).apply(instance, ParameterLocation::new));
	
	public ParameterLocation(ResourceLocation location, int weight, Either<SpaceOffsetParameters, OrbitingObjectParameters.OrbitParameters> pos)
	{
		this.location = location;
		this.weight = weight;
		this.pos = pos;
	}
	
	public ResourceLocation location()
	{
		return location;
	}
	
	public int weight()
	{
		return weight;
	}
	
	@Nullable
	public SpaceOffsetParameters spaceCoordOffset()
	{
		if(pos.left().isPresent())
			return pos.left().get();
		
		return null;
	}
	
	@Nullable
	public OrbitingObjectParameters.OrbitParameters orbitParameters()
	{
		if(pos.right().isPresent())
			return pos.right().get();
		
		return null;
	}
}
