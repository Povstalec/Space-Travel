package net.povstalec.spacetravel.common.space;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public interface GenerationObject
{
	@Nullable
	ResourceLocation getChildrenParameters();
	
	void generateChildren();
}
