package net.povstalec.spacetravel.common.space;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.data.Multiverse;
import net.povstalec.spacetravel.common.init.PacketHandlerInit;
import net.povstalec.spacetravel.common.packets.ClientBoundSpaceRegionLoadPacket;
import net.povstalec.spacetravel.common.packets.ClientBoundSpaceRegionUnloadPacket;
import net.povstalec.spacetravel.common.packets.ClientBoundSpaceshipUpdatePacket;
import net.povstalec.spacetravel.common.space.objects.OrbitingObject;
import net.povstalec.spacetravel.common.util.AxisRot;
import net.povstalec.spacetravel.common.util.SpacePos;
import net.povstalec.spacetravel.common.util.TextureLayer;

public class Spaceship extends OrbitingObject
{
	public static final ResourceLocation SPACESHIP_LOCATION = new ResourceLocation(SpaceTravel.MODID, "spaceship");
	
	private final HashMap<SpaceRegion.Position, SpaceRegion> loadedSpaceRegions = new HashMap<SpaceRegion.Position, SpaceRegion>();
	private SpaceRegion.Position spaceRegionPos;
	
	private int xAxisSpeed = 0;
	private int yAxisSpeed = 0;
	private int zAxisSpeed = 0;
	
	private double xAxisRotation = 0;
	private double yAxisRotation = 0;
	private double zAxisRotation = 0;
	
	public Spaceship()
	{
		super(SPACESHIP_LOCATION, Optional.empty(), Either.left(new SpacePos()), new AxisRot(), FadeOutHandler.DEFAULT_PLANET_HANDLER, new ArrayList<TextureLayer>(), Optional.empty());
		
		spaceRegionPos = new SpaceRegion.Position(this.getSpaceCoords());
	}

	public int getxAxisSpeed() { return xAxisSpeed; }
	public int getyAxisSpeed() { return yAxisSpeed; }
	public int getzAxisSpeed() { return zAxisSpeed; }
	public double getxAxisRotation() { return xAxisRotation; }
	public double getyAxisRotation() { return yAxisRotation; }
	public double getzAxisRotation() { return zAxisRotation; }
	
	public void setSpeed(int xAxisSpeed, int yAxisSpeed, int zAxisSpeed)
	{
		this.xAxisSpeed = xAxisSpeed;
		this.yAxisSpeed = yAxisSpeed;
		this.zAxisSpeed = zAxisSpeed;
	}
	
	public SpacePos getSpaceCoords()
	{
		return this.coords;
	}
	
	public void rotate(double xAxisRotation, double yAxisRotation, double zAxisRotation)
	{
		this.xAxisRotation = Math.toRadians(xAxisRotation);
		this.yAxisRotation = Math.toRadians(yAxisRotation);
		this.zAxisRotation = Math.toRadians(zAxisRotation);
	}
	
	public void travel(ServerLevel level)
	{
		this.coords = this.coords.add(new SpacePos(xAxisSpeed, yAxisSpeed, zAxisSpeed));

		this.axisRot = this.axisRot.add(xAxisRotation, yAxisRotation, zAxisRotation);
		
		SpaceRegion.Position spaceRegionPos = new SpaceRegion.Position(this.getSpaceCoords());
		if(!this.spaceRegionPos.equals(spaceRegionPos))
		{
			this.spaceRegionPos = spaceRegionPos;
			
			// Space Region loading and unloading
			Map<SpaceRegion.Position, SpaceRegion> newSpaceRegions = newSpaceRegions(level.getServer(), spaceRegionPos);
			
			for(SpaceRegion.Position regionPos : regionsToLoad(newSpaceRegions))
			{
				SpaceRegion spaceRegion = newSpaceRegions.get(regionPos);
				loadedSpaceRegions.put(regionPos, spaceRegion);
				PacketHandlerInit.sendPacketToDimension(level.dimension(), new ClientBoundSpaceRegionLoadPacket(spaceRegion));
			}
			
			for(SpaceRegion.Position regionPos : regionsToUnload(newSpaceRegions))
			{
				loadedSpaceRegions.remove(regionPos);
				PacketHandlerInit.sendPacketToDimension(level.dimension(), new ClientBoundSpaceRegionUnloadPacket(regionPos));
			}
		}
	}
	
	@Nullable
	private Map<SpaceRegion.Position, SpaceRegion> newSpaceRegions(MinecraftServer server, SpaceRegion.Position spaceRegionPos)
	{
		Optional<Universe> universe = Multiverse.get(server).getUniverse(Multiverse.PRIME_UNIVERSE);
		
		if(universe.isPresent())
			return universe.get().getRegionsAt(spaceRegionPos, SpaceRegion.SPACE_REGION_LOAD_DISTANCE, true);
		
		return null;
	}
	
	private List<SpaceRegion.Position> regionsToUnload(Map<SpaceRegion.Position, SpaceRegion> newSpaceRegions)
	{
		ArrayList<SpaceRegion.Position> unloadedRegions = new ArrayList<SpaceRegion.Position>();
		
		for(Map.Entry<SpaceRegion.Position, SpaceRegion> regionEntry : loadedSpaceRegions.entrySet())
		{
			if(!newSpaceRegions.containsKey(regionEntry.getKey()))
				unloadedRegions.add(regionEntry.getKey());
		}
		
		return unloadedRegions;
	}
	
	private List<SpaceRegion.Position> regionsToLoad(Map<SpaceRegion.Position, SpaceRegion> newSpaceRegions)
	{
		ArrayList<SpaceRegion.Position> loadedRegions = new ArrayList<SpaceRegion.Position>();
		
		for(Map.Entry<SpaceRegion.Position, SpaceRegion> regionEntry : newSpaceRegions.entrySet())
		{
			if(!loadedSpaceRegions.containsKey(regionEntry.getKey()))
				loadedRegions.add(regionEntry.getKey());
		}
		
		return loadedRegions;
	}
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = super.serializeNBT();
		//TODO
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		super.deserializeNBT(tag);
		
		//TODO
	}
	
	public void tick(ServerLevel level)
	{
		travel(level);
		
		PacketHandlerInit.sendPacketToDimension(level.dimension(), new ClientBoundSpaceshipUpdatePacket(this));
	}
	
	public void updateClientSpaceship()
	{
		
	}
	
	public void updateClientRegions()
	{
		
	}
}
