package net.povstalec.spacetravel.common.space;

import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.common.space.generation.SpaceObjectParameterRegistry;
import net.povstalec.spacetravel.common.space.generation.parameters.SpaceObjectParameters;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;

import javax.annotation.Nullable;

public interface GenerationObject
{
	String GENERATION_PARAMETERS = "generation_parameters";
	
	@Nullable
	ResourceLocation getGenerationParameters();
	
	void setGenerationParameters(ResourceLocation generationParameters);
	
	long generationSeed();
	
	SpaceObject generationParent();
	
	default void generateChildren()
	{
		ResourceLocation parameterLocation = getGenerationParameters();
		if(parameterLocation == null)
			return;
		
		SpaceObjectParameters parameters = SpaceObjectParameterRegistry.get(parameterLocation);
		if(parameters == null)
			return;
		
		parameters.generateChildrenAfterParent(generationParent(), generationSeed());
		setGenerationParameters(null);
	}
}
