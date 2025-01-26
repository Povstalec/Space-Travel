package net.povstalec.spacetravel.common.space.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.common.space.generation.parameters.SpaceObjectParameters;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParameterLocations
{
	public static final String COUNT = "count";
	public static final String PARAMETERS = "parameters";
	
	protected SpaceTravelParameters.IntRange count;
	protected ArrayList<ParameterLocation> parameters;
	protected int parametersWeight;
	
	public static final Codec<ParameterLocations> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SpaceTravelParameters.IntRange.codec(0, Integer.MAX_VALUE).fieldOf(COUNT).forGetter(parameters -> parameters.count),
			ParameterLocation.CODEC.listOf().fieldOf(PARAMETERS).forGetter(parameters -> parameters.parameters)
	).apply(instance, ParameterLocations::new));
	
	public ParameterLocations(SpaceTravelParameters.IntRange count, List<ParameterLocation> parameters)
	{
		this.parameters = new ArrayList<>(parameters);
		this.parametersWeight = 0;
		for(ParameterLocation childTemplate : parameters)
		{
			parametersWeight += childTemplate.weight();
		}
		
		this.count = count;
	}
	
	public SpaceTravelParameters.IntRange count()
	{
		return count;
	}
	
	@Nullable
	public ParameterLocation randomParameterLocation(Random random)
	{
		if(parameters.isEmpty())
			return null;
		
		int i = 0;
		
		for(int weight = random.nextInt(0, parametersWeight); i < parameters.size() - 1; i++)
		{
			weight -= parameters.get(i).weight();
			
			if(weight <= 0)
				break;
		}
		
		return parameters.get(i);
	}
}
