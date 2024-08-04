package net.povstalec.spacetravel.common.space;

import java.util.ArrayList;
import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.space.objects.OrbitingObject;
import net.povstalec.spacetravel.common.util.AxisRotation;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import net.povstalec.spacetravel.common.util.TextureLayer;

public class Spaceship extends OrbitingObject
{
	public static final ResourceLocation SPACESHIP_LOCATION = new ResourceLocation(SpaceTravel.MODID, "spaceship");
	
	private int speed = 0;
	
	public Spaceship()
	{
		super(SPACESHIP_LOCATION, Optional.empty(), new SpaceCoords(), new AxisRotation(), Optional.empty(), new ArrayList<TextureLayer>());
	}
	
	public void toggleSpeed()
	{
		if(speed == 0)
			speed = 100;
		else
			speed = 0;
	}
	
	public void travel()
	{
		if(speed != 0)
			this.coords = this.coords.add(new SpaceCoords(0, 0, -speed));
	}
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();
		
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		
	}
}
