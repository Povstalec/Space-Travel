package net.povstalec.spacetravel.common.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * TODO Explain what this is for
 */
public class SpaceshipCapability implements INBTSerializable<CompoundTag>
{
	

	@Override
	public CompoundTag serializeNBT()
	{
		return new CompoundTag();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		
	}
}
