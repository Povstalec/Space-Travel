package net.povstalec.spacetravel.common.data;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.space.Universe;

public class Space extends SavedData
{
	private static final String FILE_NAME = SpaceTravel.MODID + "-space";

	private static final String UNIVERSES = "universes";

	private HashMap<String, Universe> universes = new HashMap<String, Universe>(); // TODO Maybe change it from String to something better?
	
	private MinecraftServer server;
	
	/*public final void updateData(MinecraftServer server)
	{
		eraseData(server); //TODO Does this really need any erasing?
		
		registerSpaceObjectFromDataPacks(server);
	}

	public void eraseData(MinecraftServer server)
	{
		this.spaceObjects.clear();
		this.setDirty();
	}*/
	
	public void setupUniverse()
	{
		if(universes.containsKey("main"))
			System.out.println("--- Already contains main");
		else
		{
			universes.put("main", new Universe());
			System.out.println("--- Created new main");
		}
		
		this.setDirty();
	}

	//============================================================================================
	//*********************************Registering Space Objects**********************************
	//============================================================================================
	
	

	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================

	private CompoundTag serialize()
	{
		CompoundTag tag = new CompoundTag();
		
		tag.put(UNIVERSES, serializeUniverses());
		
		return tag;
	}

	private CompoundTag serializeUniverses()
	{
		CompoundTag tag = new CompoundTag();
		
		for(Map.Entry<String, Universe> universeEntry : universes.entrySet())
		{
			tag.put(universeEntry.getKey().toString(), universeEntry.getValue().serializeNBT());
		}
		
		return tag;
	}
	
	private void deserialize(CompoundTag tag)
	{
		deserializeUniverses(tag.getCompound(UNIVERSES));
	}

	private void deserializeUniverses(CompoundTag tag)
	{
		SpaceTravel.LOGGER.info("Deserializing Universes");
		
		for(String name : tag.getAllKeys())
		{
			Universe universe = new Universe();
			universe.deserializeNBT(tag.getCompound(name));
			
			universes.put(name, universe);
			
			SpaceTravel.LOGGER.info("Deserialized " + name);
		}

	}
	
	//============================================================================================
	//********************************************Data********************************************
	//============================================================================================

	public Space(MinecraftServer server)
	{
		this.server = server;
	}
	
	public static Space create(MinecraftServer server)
	{
		return new Space(server);
	}
	
	public static Space load(MinecraftServer server, CompoundTag tag)
	{
		Space data = create(server);

		data.server = server;
		data.deserialize(tag);
		
		return data;
	}
	
	public CompoundTag save(CompoundTag tag)
	{
		tag = serialize();
		
		return tag;
	}
	
	@Nonnull
	public static Space get(Level level)
	{
		if(level.isClientSide())
			throw new RuntimeException("Don't access this client-side!");
		
		return Space.get(level.getServer());
	}
	
	@Nonnull
	public static Space get(MinecraftServer server)
	{
		DimensionDataStorage storage = server.overworld().getDataStorage();
		
		return storage.computeIfAbsent((tag) -> load(server, tag), () -> create(server), FILE_NAME);
	}
}
