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
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.data.Multiverse;
import net.povstalec.spacetravel.common.init.PacketHandlerInit;
import net.povstalec.spacetravel.common.packets.ClientBoundSpaceRegionLoadPacket;
import net.povstalec.spacetravel.common.packets.ClientBoundSpaceRegionUnloadPacket;
import net.povstalec.spacetravel.common.packets.ClientBoundSpaceshipUpdatePacket;
import net.povstalec.stellarview.api.common.SpaceRegion;
import net.povstalec.stellarview.api.common.space_objects.ViewObject;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.TextureLayer;

public class Spaceship extends ViewObject
{
	private final HashMap<SpaceRegion.RegionPos, SpaceRegion> loadedSpaceRegions = new HashMap<SpaceRegion.RegionPos, SpaceRegion>();
	private SpaceRegion.RegionPos spaceRegionPos;
	
	private int xAxisSpeed = 0;
	private int yAxisSpeed = 0;
	private int zAxisSpeed = 0;
	
	private double xAxisRotation = 0;
	private double yAxisRotation = 0;
	private double zAxisRotation = 0;
	
	public Spaceship()
	{
		super(Optional.empty(), Either.left(new SpaceCoords()), new AxisRotation(), Optional.empty(), new ArrayList<TextureLayer>(), FadeOutHandler.DEFAULT_PLANET_HANDLER);
		
		spaceRegionPos = new SpaceRegion.RegionPos(this.getSpaceCoords());
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
	
	public void setPos(ServerLevel level, long x, long y, long z)
	{
		this.coords = new SpaceCoords(x, y, z);
		spaceRegionPos = new SpaceRegion.RegionPos(this.getSpaceCoords());
		
		updateRegions(level);
	}
	
	public SpaceCoords getSpaceCoords()
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
		this.coords = this.coords.add(new SpaceCoords(xAxisSpeed, yAxisSpeed, zAxisSpeed));

		this.axisRotation = this.axisRotation.add(xAxisRotation, yAxisRotation, zAxisRotation);
		
		updateRegions(level);
	}
	
	private void updateRegions(ServerLevel level)
	{
		SpaceRegion.RegionPos spaceRegionPos = new SpaceRegion.RegionPos(this.getSpaceCoords());
		if(!this.spaceRegionPos.equals(spaceRegionPos))
		{
			this.spaceRegionPos = spaceRegionPos;
			
			// Space Region loading and unloading
			Map<SpaceRegion.RegionPos, STSpaceRegion> newSpaceRegions = newSpaceRegions(level.getServer(), spaceRegionPos);
			
			for(SpaceRegion.RegionPos regionPos : regionsToLoad(newSpaceRegions))
			{
				STSpaceRegion spaceRegion = newSpaceRegions.get(regionPos);
				loadedSpaceRegions.put(regionPos, spaceRegion);
				spaceRegion.load(level.getServer()); // Loads additional information for all the loadable objects
				PacketHandlerInit.sendPacketToDimension(level.dimension(), new ClientBoundSpaceRegionLoadPacket(spaceRegion));
			}
			
			for(SpaceRegion.RegionPos regionPos : regionsToUnload(newSpaceRegions))
			{
				loadedSpaceRegions.remove(regionPos);
				PacketHandlerInit.sendPacketToDimension(level.dimension(), new ClientBoundSpaceRegionUnloadPacket(regionPos));
			}
		}
	}
	
	@Nullable
	private Map<SpaceRegion.RegionPos, STSpaceRegion> newSpaceRegions(MinecraftServer server, SpaceRegion.RegionPos spaceRegionPos)
	{
		Optional<Universe> universe = Multiverse.get(server).getUniverse(Multiverse.PRIME_UNIVERSE);
		
		if(universe.isPresent())
			return universe.get().getRegionsAt(spaceRegionPos, STSpaceRegion.SPACE_REGION_LOAD_DISTANCE, true);
		
		return null;
	}
	
	private List<SpaceRegion.RegionPos> regionsToUnload(Map<SpaceRegion.RegionPos, STSpaceRegion> newSpaceRegions)
	{
		ArrayList<SpaceRegion.RegionPos> unloadedRegions = new ArrayList<SpaceRegion.RegionPos>();
		
		for(Map.Entry<SpaceRegion.RegionPos, SpaceRegion> regionEntry : loadedSpaceRegions.entrySet())
		{
			if(!newSpaceRegions.containsKey(regionEntry.getKey()))
				unloadedRegions.add(regionEntry.getKey());
		}
		
		return unloadedRegions;
	}
	
	private List<SpaceRegion.RegionPos> regionsToLoad(Map<SpaceRegion.RegionPos, STSpaceRegion> newSpaceRegions)
	{
		if(newSpaceRegions == null)
			return new ArrayList<>();
		
		ArrayList<SpaceRegion.RegionPos> loadedRegions = new ArrayList<SpaceRegion.RegionPos>();
		
		for(Map.Entry<SpaceRegion.RegionPos, STSpaceRegion> regionEntry : newSpaceRegions.entrySet())
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
	
	public void beamDown(Player player, ServerLevel level)
	{
		Optional<Universe> universe = Multiverse.get(level).getUniverse(Multiverse.PRIME_UNIVERSE);
		if(player != null && universe.isPresent())
		{
			STSpaceRegion region = universe.get().getRegionAt(new SpaceRegion.RegionPos(getSpaceCoords()), false);
			DimensionObject dimensionObject = (DimensionObject) region.findClosest(getSpaceCoords(), spaceObject -> spaceObject instanceof DimensionObject dimObject && dimObject.hasSurface());
			if(dimensionObject != null)
			{
				ServerLevel dimensionLevel = dimensionObject.getLevel(level.getServer(), true);
				if(dimensionLevel != null)
					player.teleportTo(dimensionLevel,
							player.getX(), dimensionLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, player.getOnPos().getX(), player.getOnPos().getZ()),
							player.getZ(), RelativeMovement.ALL,
							player.getYRot(), player.getXRot());
			}
		}
	}
	
	public void updateClientSpaceship()
	{
		
	}
	
	public void updateClientRegions()
	{
		
	}
}
