package net.povstalec.spacetravel.common.capabilities;

import net.povstalec.spacetravel.common.space.Spaceship;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class SpaceshipCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag>
{
	public static final Capability<SpaceshipCapability> SPACESHIP = CapabilityManager.get(new CapabilityToken<SpaceshipCapability>() {});
	private SpaceshipCapability spaceshipCapability = null;
	private final LazyOptional<SpaceshipCapability> optional = LazyOptional.of(this::getOrCreateCapability);
	private Spaceship spaceship;
	
	public SpaceshipCapabilityProvider(Spaceship spaceship)
	{
		this.spaceship = spaceship;
	}
	
	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		if(cap == SPACESHIP)
			return optional.cast();
		return LazyOptional.empty();
	}
	
	private SpaceshipCapability getOrCreateCapability()
	{
		if(this.spaceshipCapability == null)
			this.spaceshipCapability = new SpaceshipCapability(this.spaceship);
		return this.spaceshipCapability;
	}

	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();
		getOrCreateCapability().saveData(tag);
		
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		getOrCreateCapability().loadData(tag);
	}
}
