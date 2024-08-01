package net.povstalec.spacetravel.common.init;

import com.mojang.serialization.Codec;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.worldgen.SpaceChunkGenerator;

public class WorldGenInit
{
    public static final ResourceKey<DimensionType> SPACE_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, new ResourceLocation(SpaceTravel.MODID, "space"));
    //TODO public static final ResourceKey<DimensionType> PLANET_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, new ResourceLocation(SpaceTravel.MODID, "planet"));

    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(Registries.CHUNK_GENERATOR, SpaceTravel.MODID);

    public static final RegistryObject<Codec<? extends ChunkGenerator>> SPACE_CHUNK_GENERATOR = CHUNK_GENERATORS.register("space", () -> SpaceChunkGenerator.CODEC);
  //TODO public static final RegistryObject<Codec<? extends ChunkGenerator>> PLANET_CHUNK_GENERATOR = CHUNK_GENERATORS.register("planet", () -> PlanetChunkGenerator.CODEC);

    public static final ResourceKey<Biome> SPACE_BIOME = ResourceKey.create(Registries.BIOME, new ResourceLocation(SpaceTravel.MODID, "space"));

    public static void register(IEventBus bus)
    {
        CHUNK_GENERATORS.register(bus);
    }
}
