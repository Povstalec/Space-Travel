package net.povstalec.spacetravel.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DustCloudInfo implements INBTSerializable<CompoundTag>
{
	public static final String DUST_CLOUD_TYPES = "dust_cloud_types";
	public static final String TOTAL_WEIGHT = "total_weight";
	
	private ArrayList<DustCloudType> dustCloudTypes;
	private int totalWeight = 0;
	
	public static final DustCloudType WHITE_DUST_CLOUD = new DustCloudType(new Color.IntRGB(107, 107, 107), 2.0F, 7.0F, (short) 255, (short) 255, 1);
	public static final List<DustCloudType> DEFAULT_DUST_CLOUDS = Arrays.asList(WHITE_DUST_CLOUD);
	public static final DustCloudInfo DEFAULT_DUST_CLOUD_INFO = new DustCloudInfo(DEFAULT_DUST_CLOUDS);
	
	public static final Codec<DustCloudInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			DustCloudType.CODEC.listOf().fieldOf("dust_cloud_types").forGetter(dustCloudInfo -> dustCloudInfo.dustCloudTypes)
	).apply(instance, DustCloudInfo::new));
	
	public DustCloudInfo() {}
	
	public DustCloudInfo(List<DustCloudType> dustCloudTypes)
	{
		this.dustCloudTypes = new ArrayList<DustCloudType>(dustCloudTypes);
		
		for(DustCloudType dustCloudType : dustCloudTypes)
		{
			this.totalWeight += dustCloudType.getWeight();
		}
	}
	
	public DustCloudType getRandomDustCloudType(long seed)
	{
		if(dustCloudTypes.isEmpty())
			return WHITE_DUST_CLOUD;
		
		Random random = new Random(seed);
		
		int i = 0;
		
		for(int weight = random.nextInt(0, totalWeight); i < dustCloudTypes.size() - 1; i++)
		{
			weight -= dustCloudTypes.get(i).getWeight();
			
			if(weight <= 0)
				break;
		}
		
		return dustCloudTypes.get(i);
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();
		
		CompoundTag dustCloudTypesTag = new CompoundTag();
		for(int i = 0; i < dustCloudTypes.size(); i++)
		{
			dustCloudTypesTag.put("star_type_" + i, dustCloudTypes.get(i).serializeNBT());
		}
		tag.put(DUST_CLOUD_TYPES, dustCloudTypesTag);
		
		tag.putInt(TOTAL_WEIGHT, totalWeight);
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		this.dustCloudTypes = new ArrayList<DustCloudType>();
		CompoundTag dustCloudTypesTag = tag.getCompound(DUST_CLOUD_TYPES);
		for(String key : dustCloudTypesTag.getAllKeys())
		{
			DustCloudType dustCloudType = new DustCloudType();
			dustCloudType.deserializeNBT(dustCloudTypesTag.getCompound(key));
			this.dustCloudTypes.add(dustCloudType);
			
		}
		
		totalWeight = tag.getInt(TOTAL_WEIGHT);
	}
	
	
	
	public static class DustCloudType implements INBTSerializable<CompoundTag>
	{
		public static final String RGB = "rgb";
		public static final String MAX_SIZE = "max_size";
		public static final String MIN_SIZE = "min_size";
		public static final String MAX_BRIGHTNESS = "max_brightness";
		public static final String MIN_BRIGHTNESS = "min_brightness";
		public static final String WEIGHT = "weight";
		
		private Color.IntRGB rgb;
		
		private float minSize;
		private float maxSize;
		
		private short minBrightness;
		private short maxBrightness;
		
		public int weight;
		
		public static final Codec<DustCloudType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Color.IntRGB.CODEC.fieldOf(RGB).forGetter(DustCloudType::getRGB),
				
				Codec.FLOAT.fieldOf(MIN_SIZE).forGetter(dustCloudType -> dustCloudType.minSize),
				Codec.FLOAT.fieldOf(MAX_SIZE).forGetter(dustCloudType -> dustCloudType.maxSize),
				
				Codec.SHORT.fieldOf(MIN_BRIGHTNESS).forGetter(dustCloudType -> dustCloudType.minBrightness),
				Codec.SHORT.fieldOf(MAX_BRIGHTNESS).forGetter(dustCloudType -> dustCloudType.maxBrightness),
				
				Codec.intRange(1, Integer.MAX_VALUE).fieldOf(WEIGHT).forGetter(DustCloudType::getWeight)
		).apply(instance, DustCloudType::new));
		
		public DustCloudType() {}
		
		public DustCloudType(Color.IntRGB rgb, float minSize, float maxSize, short minBrightness, short maxBrightness, int weight)
		{
			this.rgb = rgb;
			
			this.minSize = minSize;
			this.maxSize = maxSize;
			
			this.minBrightness = minBrightness;
			this.maxBrightness = (short) (maxBrightness + 1);
			
			this.weight = weight;
		}
		
		public Color.IntRGB getRGB() // TODO Maybe random RGB?
		{
			return rgb;
		}
		
		public int getWeight()
		{
			return weight;
		}
		
		public float randomSize(long seed)
		{
			if(minSize == maxSize)
				return maxSize;
			
			Random random = new Random(seed);
			
			return random.nextFloat(minSize, maxSize);
		}
		
		public short randomBrightness(long seed)
		{
			if(minBrightness == maxBrightness)
				return maxBrightness;
			
			Random random = new Random(seed);
			
			return (short) random.nextInt(minBrightness, maxBrightness);
		}
		
		//============================================================================================
		//*************************************Saving and Loading*************************************
		//============================================================================================
		
		@Override
		public CompoundTag serializeNBT()
		{
			CompoundTag tag = new CompoundTag();
			
			tag.put(RGB, rgb.serializeNBT());
			
			tag.putFloat(MIN_SIZE, minSize);
			tag.putFloat(MAX_SIZE, maxSize);
			
			tag.putShort(MIN_BRIGHTNESS, minBrightness);
			tag.putShort(MAX_BRIGHTNESS, maxBrightness);
			
			tag.putInt(WEIGHT, weight);
			
			return tag;
		}
		
		@Override
		public void deserializeNBT(CompoundTag tag)
		{
			rgb = new Color.IntRGB();
			rgb.deserializeNBT(tag.getCompound(RGB));
			
			minSize = tag.getFloat(MIN_SIZE);
			maxSize = tag.getFloat(MAX_SIZE);
			
			minBrightness = tag.getShort(MIN_BRIGHTNESS);
			maxBrightness = tag.getShort(MAX_BRIGHTNESS);
			
			weight = tag.getInt(WEIGHT);
		}
	}
}
