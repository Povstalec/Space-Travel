package net.povstalec.spacetravel.common.space.generation;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.common.space.generation.templates.OrbitingObjectParameters;
import net.povstalec.stellarview.common.util.SpaceCoords;

import javax.annotation.Nullable;

public class ParameterLocation
{
	public static final String PARAMETERS = "parameters";
	public static final String WEIGHT = "weight";
	public static final String COUNT = "count";
	public static final String POSITION = "position";
	
	private final ResourceLocation location;
	private final int weight;
	private final SpaceTravelParameters.IntRange count;
	
	private Either<SpaceCoords, OrbitingObjectParameters.OrbitParameters> pos;
	
	public static final Codec<ParameterLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf(PARAMETERS).forGetter(info -> info.location),
			Codec.INT.fieldOf(WEIGHT).forGetter(info -> info.weight),
			
			SpaceTravelParameters.IntRange.codec(0, Integer.MAX_VALUE).optionalFieldOf(COUNT, new SpaceTravelParameters.IntRange(0, 0)).forGetter(info -> info.count),
			
			Codec.either(SpaceCoords.CODEC, OrbitingObjectParameters.OrbitParameters.CODEC).optionalFieldOf(POSITION, Either.left(new SpaceCoords())).forGetter(info -> info.pos)
	).apply(instance, ParameterLocation::new));
	
	public ParameterLocation(ResourceLocation location, int weight, SpaceTravelParameters.IntRange count, Either<SpaceCoords, OrbitingObjectParameters.OrbitParameters> pos)
	{
		this.location = location;
		this.weight = weight;
		this.count = count;
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
	
	public SpaceTravelParameters.IntRange count()
	{
		return count;
	}
	
	public SpaceCoords spaceCoordOffset()
	{
		if(pos.left().isPresent())
			return pos.left().get();
		
		return new SpaceCoords();
	}
	
	@Nullable
	public OrbitingObjectParameters.OrbitParameters orbitParameters()
	{
		if(pos.right().isPresent())
			return pos.right().get();
		
		return null;
	}
}
