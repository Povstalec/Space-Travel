package net.povstalec.spacetravel.common.space.generation.parameters;

import net.povstalec.spacetravel.common.space.generation.ParameterLocation;
import net.povstalec.spacetravel.common.space.generation.ParameterLocations;
import net.povstalec.spacetravel.common.space.generation.SpaceObjectParameterRegistry;
import net.povstalec.spacetravel.common.space.generation.SpaceOffsetParameters;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class SpaceObjectParameters<T extends SpaceObject>
{
	public static final String CHILDREN = "child_parameters";
	public static final String INSTANT_CHILDREN = "instant_child_parameters";
	
	@Nullable
	protected ArrayList<ParameterLocations> childrenParameters; // These generate when player is close
	@Nullable
	protected ArrayList<ParameterLocations> instantChildrenParameters; // These generate along with the parent
	
	public SpaceObjectParameters(Optional<List<ParameterLocations>> instantChildrenParameters, Optional<List<ParameterLocations>> childrenParameters)
	{
		this.instantChildrenParameters = instantChildrenParameters.isPresent() ? new ArrayList<>(instantChildrenParameters.get()) : null;
		this.childrenParameters = childrenParameters.isPresent() ? new ArrayList<>(childrenParameters.get()) : null;
	}
	
	
	
	public abstract T generate(Random random, long seed, SpaceCoords spaceCoords, AxisRotation axisRotation);
	
	
	
	protected void generateChild(SpaceObject parent, ParameterLocations locations, Random random, long seed)
	{
		ParameterLocation parameterLocation = locations.randomParameterLocation(random);
		if(parameterLocation == null)
			return;
		
		SpaceObjectParameters parameters = SpaceObjectParameterRegistry.get(parameterLocation.location());
		if(parameters == null)
			return;
		
		//TODO Randomize coords and axis rotation
		SpaceOffsetParameters offset = parameterLocation.spaceCoordOffset();
		
		SpaceObject child = parameters.generate(random, seed, offset.randomOffset(random), new AxisRotation());
		if(child != null)
			parent.addChild(child);
	}
	
	protected void generateChildren(SpaceObject parent, ParameterLocations locations, Random random, long seed)
	{
		int count = locations.count().nextInt(random);
		for(int i = 0; i < count; i++)
		{
			generateChild(parent, locations, random, seed);
		}
	}
	
	protected void generateChildrenWithParent(SpaceObject parent, long seed)
	{
		if(instantChildrenParameters == null || instantChildrenParameters.isEmpty())
			return;
		
		Random random = new Random(seed);
		for(ParameterLocations locations : instantChildrenParameters)
		{
			generateChildren(parent, locations, random, seed);
		}
	}
	
	public void generateChildrenAfterParent(SpaceObject parent, long seed)
	{
		if(childrenParameters == null || childrenParameters.isEmpty())
			return;
		
		Random random = new Random(seed);
		for(ParameterLocations locations : childrenParameters)
		{
			generateChild(parent, locations, random, seed);
		}
	}
}
