package net.povstalec.spacetravel.common.space.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public class WeightedStarInfo
{
	public static final String STAR_INFO = "star_info";
	public static final String WEIGHT = "weight";
	
	private final ResourceLocation starInfo;
	private final int weight;
	
	public static final Codec<WeightedStarInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf(STAR_INFO).forGetter(info -> info.starInfo),
			Codec.INT.fieldOf(WEIGHT).forGetter(info -> info.weight)
	).apply(instance, WeightedStarInfo::new));
	
	public WeightedStarInfo(ResourceLocation starInfo, int weight)
	{
		this.starInfo = starInfo;
		this.weight = weight;
	}
	
	public ResourceLocation starInfo()
	{
		return starInfo;
	}
	
	public int weight()
	{
		return weight;
	}
}
