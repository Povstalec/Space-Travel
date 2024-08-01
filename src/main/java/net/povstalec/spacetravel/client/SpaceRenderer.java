package net.povstalec.spacetravel.client;

import java.util.HashMap;
import java.util.Map;

import net.povstalec.spacetravel.common.space.SpaceRegion;

public class SpaceRenderer
{
private static final HashMap<SpaceRegion.Position, SpaceRegion> SPACE_REGIONS = new HashMap<SpaceRegion.Position, SpaceRegion>();
	
	public void clear()
	{
		SPACE_REGIONS.clear();
	}
	
	public void addSpaceRegion(SpaceRegion spaceRegion)
	{
		SPACE_REGIONS.put(spaceRegion.getRegionPos(), spaceRegion);
	}
	
	public void removeSpaceRegion(SpaceRegion.Position pos)
	{
		SPACE_REGIONS.remove(pos);
	}
	
	public void print()
	{
		for(Map.Entry<SpaceRegion.Position, SpaceRegion> spaceRegionEntry : SPACE_REGIONS.entrySet())
		{
			System.out.println("Space objects: " + spaceRegionEntry.getValue().toString());
		}
	}
	
	private static void renderSpaceRegion(SpaceRegion spaceRegion)
	{
		//TODO
	}
}
