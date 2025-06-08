package net.povstalec.spacetravel.common.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.povstalec.spacetravel.common.space.Spaceship;

/**
 * TODO Explain what this is for
 */
public class SpaceshipCapability
{
	public static final String SPACESHIP = "spaceship";
	
	public Spaceship spaceship;
	
	public SpaceshipCapability(Spaceship spaceship)
	{
		this.spaceship = spaceship;
	}
	
	public void saveData(CompoundTag tag)
	{
		tag.put(SPACESHIP, spaceship.serializeNBT());
	}
	
	public void loadData(CompoundTag tag)
	{
		spaceship.deserializeNBT(tag.getCompound(SPACESHIP));
	}
}
