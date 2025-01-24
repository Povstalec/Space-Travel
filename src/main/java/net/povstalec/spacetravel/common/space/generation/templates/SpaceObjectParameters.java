package net.povstalec.spacetravel.common.space.generation.templates;

import net.povstalec.spacetravel.common.space.generation.ParameterLocation;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class SpaceObjectParameters<T extends SpaceObject>
{
	public static final String CHILDREN = "children_parameters";
	
	protected ArrayList<ParameterLocation> childrenParameters;
	protected int childrenWeight;
	
	public SpaceObjectParameters(List<ParameterLocation> childrenParameters)
	{
		this.childrenParameters = new ArrayList<>(childrenParameters);
		for(ParameterLocation childTemplate : childrenParameters)
		{
			childrenWeight += childTemplate.weight();
		}
	}
	
	
	
	public abstract T generate(Random random, long seed, SpaceCoords spaceCoords, AxisRotation axisRotation);
	
	/*@Nullable
	protected SpaceObjectParameters randomChildTemplate(Random random)
	{
		if(children.isEmpty())
			return null;
		
		int i = 0;
		
		for(int weight = random.nextInt(0, childrenWeight); i < children.size() - 1; i++)
		{
			weight -= children.get(i).weight();
			
			if(weight <= 0)
				break;
		}
		
		return SpaceObjectTemplateRegistry.get(children.get(i).location());
	}*/
}
