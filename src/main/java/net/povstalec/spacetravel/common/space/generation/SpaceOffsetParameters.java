package net.povstalec.spacetravel.common.space.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.povstalec.stellarview.common.util.SpaceCoords;

import java.util.Random;

public class SpaceOffsetParameters
{
	protected SpaceCoords.SpaceDistance minDistance;
	protected SpaceCoords.SpaceDistance maxDistance;
	
	public static final Codec<SpaceOffsetParameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SpaceCoords.SpaceDistance.CODEC.fieldOf("min").forGetter(parameters -> parameters.maxDistance),
			SpaceCoords.SpaceDistance.CODEC.fieldOf("max").forGetter(parameters -> parameters.maxDistance)
	).apply(instance, SpaceOffsetParameters::new));
	
	public SpaceOffsetParameters()
	{
		this.minDistance = new SpaceCoords.SpaceDistance();
		this.maxDistance = new SpaceCoords.SpaceDistance(1);
	}
	
	public SpaceOffsetParameters(SpaceCoords.SpaceDistance minDistance, SpaceCoords.SpaceDistance maxDistance)
	{
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
	}
	
	public SpaceCoords randomOffset(Random random)
	{
		long x = random.nextLong(minDistance.ly(), maxDistance.ly());
		if(random.nextBoolean())
			x = -x;
		long y = random.nextLong(minDistance.ly(), maxDistance.ly());
		if(random.nextBoolean())
			y = -y;
		long z = random.nextLong(minDistance.ly(), maxDistance.ly());
		if(random.nextBoolean())
			z = -z;
		
		return new SpaceCoords(x, y, z);
	}
}
