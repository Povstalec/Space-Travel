package net.povstalec.spacetravel.common.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.povstalec.spacetravel.common.space.STSpaceRegion;
import net.povstalec.spacetravel.common.space.Universe;
import net.povstalec.stellarview.api.common.SpaceRegion;
import net.povstalec.stellarview.api.common.space_objects.ViewObject;
import net.povstalec.stellarview.common.util.SpaceCoords;
import org.jetbrains.annotations.Nullable;

/**
 * TODO Explain what this is for
 */
public class ViewObjectCapability
{
	public static final String COORDS = "coords";
	
	@Nullable
	protected SpaceCoords coords;
	@Nullable
	protected ViewObject viewObject;
	
	public ViewObjectCapability(@Nullable ViewObject viewObject)
	{
		this.viewObject = viewObject;
	}
	
	public void setViewObject(@Nullable ViewObject viewObject)
	{
		this.viewObject = viewObject;
		this.coords = viewObject.getCoords();
	}
	
	public ViewObject viewObject()
	{
		return viewObject;
	}
	
	public void loadRegion(MinecraftServer server, Universe universe)
	{
		if(this.coords == null)
			return;
		
		universe.getRegionAt(new SpaceRegion.RegionPos(this.coords), false).load(server);
	}
	
	
	public void saveData(CompoundTag tag)
	{
		if(this.viewObject != null)
			tag.put(COORDS, this.viewObject.getCoords().serializeNBT());
		else if(this.coords != null)
			tag.put(COORDS, this.coords.serializeNBT());
	}
	
	public void loadData(CompoundTag tag)
	{
		if(tag.contains(COORDS))
		{
			this.coords = new SpaceCoords();
			this.coords.deserializeNBT(tag.getCompound(COORDS));
		}
	}
}
