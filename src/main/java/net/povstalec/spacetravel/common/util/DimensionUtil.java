package net.povstalec.spacetravel.common.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.serialization.Lifecycle;

import net.minecraft.Util;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.init.PacketHandlerInit;
import net.povstalec.spacetravel.common.init.WorldGenInit;
import net.povstalec.spacetravel.common.packets.ClientBoundDimensionUpdatePacket;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;
import net.povstalec.spacetravel.common.worldgen.SpaceChunkGenerator;

public class DimensionUtil
{
	/**
	 * Class for helping with dimension generation
	 * CREDIT GOES TO: Joseph Bettendorff a.k.a. "Commoble" (https://github.com/Commoble/infiniverse)
	 */
	private static final Set<ResourceKey<Level>> VANILLA_LEVELS = Set.of(Level.OVERWORLD, Level.NETHER, Level.END);

    private Set<ResourceKey<Level>> levelsPendingUnregistration = new HashSet<>();

    /**
     * Gets a level, dynamically creating and registering one if it doesn't exist.<br>
     * The dimension registry is stored in the server's level file, all previously registered dimensions are loaded
     * and recreated and reregistered whenever the server starts.<br>
     * This can be used for making dynamic dimensions at runtime; static dimensions should be defined in json instead.<br>
     * @param server a MinecraftServer instance (you can get this from a ServerPlayerEntity or ServerWorld)
     * @param levelKey A ResourceKey for your level
     * @param dimensionFactory A function that produces a new LevelStem (dimension) instance if necessary<br>
     * If this factory is used, it should be assumed that intended dimension has not been created or registered yet,
     * so making the factory attempt to get this dimension from the server's dimension registry will fail
     * @return Returns a ServerLevel, creating and registering a world and dimension for it if the world does not already exist
     */
    public ServerLevel getOrCreateLevel(final MinecraftServer server, final ResourceKey<Level> levelKey, final Supplier<LevelStem> dimensionFactory)
    {
        // this is marked as deprecated but it's not called from anywhere and I'm not sure how old it is,
        // it's probably left over from forge's previous dimension api
        // in any case we need to get at the server's world field, and if we didn't use this getter,
        // then we'd just end up making a private-field-getter for it ourselves anyway
        @SuppressWarnings("deprecation")
        Map<ResourceKey<Level>, ServerLevel> map = server.forgeGetWorldMap();
        @Nullable ServerLevel existingLevel = map.get(levelKey);

        // if the world already exists, return it
        return existingLevel == null
                ? createAndRegisterLevel(server, map, levelKey, dimensionFactory)
                : existingLevel;
    }

    @SuppressWarnings("deprecation") // markWorldsDirty is deprecated, see below
    private static ServerLevel createAndRegisterLevel(final MinecraftServer server, final Map<ResourceKey<Level>, ServerLevel> map, final ResourceKey<Level> levelKey, Supplier<LevelStem> dimensionFactory)
    {
        // get everything we need to create the dimension and the level
        final ServerLevel overworld = server.getLevel(Level.OVERWORLD);

        // dimension keys have a 1:1 relationship with level keys, they have the same IDs as well
        final ResourceKey<LevelStem> dimensionKey = ResourceKey.create(Registries.LEVEL_STEM, levelKey.location());
        final LevelStem dimension = dimensionFactory.get();

        // the int in create() here is radius of chunks to watch, 11 is what the server uses when it initializes levels
        final ChunkProgressListener chunkProgressListener = server.progressListenerFactory.create(11);
        final Executor executor = Util.backgroundExecutor();
        final LevelStorageSource.LevelStorageAccess anvilConverter = server.storageSource;
        final WorldData worldData = server.getWorldData();
        final DerivedLevelData derivedLevelData = new DerivedLevelData(worldData, worldData.overworldData());

        // now we have everything we need to create the dimension and the level
        // this is the same order server init creates levels:
        // the dimensions are already registered when levels are created, we'll do that first
        // then instantiate level, add border listener, add to map, fire world load event

        // register the actual dimension
        Registry<LevelStem> dimensionRegistry = server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
        if (dimensionRegistry instanceof MappedRegistry<LevelStem> writableRegistry)
        {
            writableRegistry.unfreeze();
            writableRegistry.register(dimensionKey, dimension, Lifecycle.stable());
        }
        else
        {
            throw new IllegalStateException(String.format("Unable to register dimension %s -- dimension registry not writable", dimensionKey.location()));
        }

        // create the level instance
        final ServerLevel newLevel = new ServerLevel(
                server,
                executor,
                anvilConverter,
                derivedLevelData,
                levelKey,
                dimension,
                chunkProgressListener,
                worldData.isDebugWorld(),
                overworld.getSeed(), // don't need to call BiomeManager#obfuscateSeed, overworld seed is already obfuscated
                List.of(), // "special spawn list"
                // phantoms, travelling traders, patrolling/sieging raiders, and cats are overworld special spawns
                // this is always empty for non-overworld dimensions (including json dimensions)
                // these spawners are ticked when the world ticks to do their spawning logic,
                // mods that need "special spawns" for their own dimensions should implement them via tick events or other systems
                false, // "tick time", true for overworld, always false for nether, end, and json dimensions
                (RandomSequences)null // as of 1.20.1 this argument is always null in vanilla, indicating the level should load the sequence from storage
        );

        // add world border listener, for parity with json dimensions
        // the vanilla behaviour is that world borders exist in every dimension simultaneously with the same size and position
        // these border listeners are automatically added to the overworld as worlds are loaded, so we should do that here too
        // TODO if world-specific world borders are ever added, change it here too
        overworld.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(newLevel.getWorldBorder()));

        // register level
        map.put(levelKey, newLevel);

        // update forge's world cache so the new level can be ticked
        server.markWorldsDirty();

        // fire world load event
        MinecraftForge.EVENT_BUS.post(new LevelEvent.Load(newLevel));

        // update clients' dimension lists
        PacketHandlerInit.sendPacketToAll(new ClientBoundDimensionUpdatePacket(Set.of(levelKey), true));
        return newLevel;
    }


    public static ServerLevel createSpaceship(MinecraftServer server, ResourceLocation dimLoc)
    {
        ServerLevel level = createAndRegisterLevel(server, server.forgeGetWorldMap(), ResourceKey.create(Registries.DIMENSION, dimLoc), () -> createSpaceshipStem(server));

        return level;
    }

    public static ServerLevel createSpaceship(MinecraftServer server, String name)
    {
        return createSpaceship(server, new ResourceLocation(SpaceTravel.MODID, name));
    }

    public static ServerLevel createSpaceship(MinecraftServer server)
    {
        return createSpaceship(server, UUID.randomUUID().toString());
    }
    
    //TODO
    /*public static ServerLevel createPlanet(MinecraftServer server, Map.Entry<String, SpaceObject.Serializable> planet)
    {
        ServerLevel level = createAndRegisterLevel(server, server.forgeGetWorldMap(), ResourceKey.create(Registries.DIMENSION, ResourceLocation.tryParse(planet.getKey())), () -> createPlanetStem(server, planet.getValue()));

        return level;
    }*/

    public static LevelStem createSpaceshipStem(MinecraftServer server)
    {
        RegistryAccess registries = server.registryAccess();

        LevelStem stem = new LevelStem(registries.registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(WorldGenInit.SPACE_TYPE),
                new SpaceChunkGenerator(
                        registries.registryOrThrow(Registries.BIOME).asLookup()
                ));

        return stem;
    }

    //TODO
    /*public static LevelStem createPlanetStem(MinecraftServer server, SpaceObject.Serializable planet)
    {
        RegistryAccess registries = server.registryAccess();

        MultiNoiseBiomeSource multiSource = MultiNoiseBiomeSource.createFromList(planet.getSurface().getSecond());

        LevelStem stem =
                new LevelStem(
                   registries.registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(WorldGenInit.PLANET_TYPE),
                new NoiseBasedChunkGenerator(multiSource,
                   registries.registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(planet.getSurface().getFirst())));

        return stem;
    }*/
}
