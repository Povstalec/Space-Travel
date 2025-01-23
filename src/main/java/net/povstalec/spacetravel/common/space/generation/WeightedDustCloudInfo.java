package net.povstalec.spacetravel.common.space.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public class WeightedDustCloudInfo
{
	public static final String DUST_CLOUD_INFO = "dust_cloud_info";
	public static final String WEIGHT = "weight";
	
	private final ResourceLocation dustCloudInfo;
	private final int weight;
	
	public static final Codec<WeightedDustCloudInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf(DUST_CLOUD_INFO).forGetter(info -> info.dustCloudInfo),
			Codec.INT.fieldOf(WEIGHT).forGetter(info -> info.weight)
	).apply(instance, WeightedDustCloudInfo::new));
	
	public WeightedDustCloudInfo(ResourceLocation dustCloudInfo, int weight)
	{
		this.dustCloudInfo = dustCloudInfo;
		this.weight = weight;
	}
	
	public ResourceLocation dustCloudInfo()
	{
		return dustCloudInfo;
	}
	
	public int weight()
	{
		return weight;
	}
}
