package net.povstalec.spacetravel.common.capabilities;

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
	public static Capability<SpaceshipCapability> SPACESHIP = CapabilityManager.get(new CapabilityToken<SpaceshipCapability>() {});
	private SpaceshipCapability ancientGene = null;
	private final LazyOptional<SpaceshipCapability> optional = LazyOptional.of(this::getOrCreateCapability);
	
	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		if(cap == SPACESHIP)
			return optional.cast();
		return LazyOptional.empty();
	}
	
	private SpaceshipCapability getOrCreateCapability()
	{
		if(this.ancientGene == null)
			this.ancientGene = new SpaceshipCapability();
		return this.ancientGene;
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
