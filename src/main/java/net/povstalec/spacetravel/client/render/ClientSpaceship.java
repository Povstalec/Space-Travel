package net.povstalec.spacetravel.client.render;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.resourcepack.objects.OrbitingObject;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.TextureLayer;

import java.util.*;

public class ClientSpaceship extends OrbitingObject
{
	public static final String COORDS = "coords";
	public static final String AXIS_ROTATION = "axis_rotation";
	
	public static final String ID = "id";
	
	public ClientSpaceship()
	{
		super(Optional.empty(), Either.left(new SpaceCoords()), new AxisRotation(), Optional.empty(), new ArrayList<TextureLayer>(), FadeOutHandler.DEFAULT_PLANET_HANDLER);
	}
	
	public void deserializeNBT(CompoundTag tag)
	{
		if(tag.contains(ID))
			this.location = new ResourceLocation(tag.getString(ID));
		
		this.coords = SpaceCoords.fromTag(tag.getCompound(COORDS));
		
		this.axisRotation = AxisRotation.fromTag(tag.getCompound(AXIS_ROTATION));
	}
}
