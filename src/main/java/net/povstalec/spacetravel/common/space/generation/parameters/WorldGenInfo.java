package net.povstalec.spacetravel.common.space.generation.parameters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public record WorldGenInfo(ResourceKey<NoiseGeneratorSettings> settings, Climate.ParameterList<Holder<Biome>> biomes)
{
	public static final Codec<WorldGenInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceKey.codec(Registries.NOISE_SETTINGS).fieldOf("noise_settings").forGetter(info -> info.settings),
			MultiNoiseBiomeSource.DIRECT_CODEC.forGetter(info -> info.biomes)
	).apply(instance, WorldGenInfo::new));
}
