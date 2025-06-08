package net.povstalec.spacetravel.common.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.povstalec.stellarview.api.common.space_objects.ViewObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ViewObjectCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag>
{
	public static final Capability<ViewObjectCapability> VIEW_OBJECT = CapabilityManager.get(new CapabilityToken<ViewObjectCapability>() {});
	private ViewObjectCapability viewObjectCapability = null;
	private final LazyOptional<ViewObjectCapability> optional = LazyOptional.of(this::getOrCreateCapability);
	private ViewObject viewObject;
	
	public ViewObjectCapabilityProvider(ViewObject viewObject)
	{
		this.viewObject = viewObject;
	}
	
	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		if(cap == VIEW_OBJECT)
			return optional.cast();
		return LazyOptional.empty();
	}
	
	private ViewObjectCapability getOrCreateCapability()
	{
		if(this.viewObjectCapability == null)
			this.viewObjectCapability = new ViewObjectCapability(this.viewObject);
		return this.viewObjectCapability;
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
