package net.povstalec.spacetravel.common.space.objects;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.space.objects.OrbitingObject;
import net.povstalec.spacetravel.common.space.objects.SpaceObject;
import net.povstalec.spacetravel.common.util.*;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class StarLike extends OrbitingObject
{
	public static final float MIN_SIZE = 0.2F;
	
	public static final float MAX_ALPHA = 1F;
	public static final float MIN_ALPHA = MAX_ALPHA * 0.1F; // Previously used (MAX_ALPHA - 0.66F) * 2 / 5;
	
	private float minStarSize;
	
	private float maxStarAlpha;
	private float minStarAlpha;
	
	public StarLike(ResourceLocation objectType, Optional<String> parentName, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
					Optional<OrbitingObject.OrbitInfo> orbitInfo, List<TextureLayer> textureLayers, float minStarSize, float maxStarAlpha, float minStarAlpha)
	{
		super(objectType, parentName, coords, axisRotation, orbitInfo, textureLayers);
		
		this.minStarSize = minStarSize;
		this.maxStarAlpha = maxStarAlpha;
		this.minStarAlpha = minStarAlpha;
	}
	
	public float getMinStarSize()
	{
		return minStarSize;
	}
	
	public float getMaxStarAlpha()
	{
		return maxStarAlpha;
	}
	
	public float getMinStarAlpha()
	{
		return minStarAlpha;
	}
	
	public float starSize(float size, double lyDistance)
	{
		size -= size * lyDistance / 1000000.0;
		
		if(size < getMinStarSize())
			return getMinStarSize();
		
		return size;
	}
	
	/*TODO public Color.FloatRGBA starRGBA(double lyDistance)
	{
		float alpha = getMaxStarAlpha();
		
		alpha -= lyDistance / 100000;
		
		if(alpha < getMinStarAlpha())
			alpha = getMinStarAlpha();
		
		return new Color.FloatRGBA(1, 1, 1, alpha);
	}*/
	
	public static class StarType implements INBTSerializable<CompoundTag>
	{
		public static final String RGB = "rgb";
		public static final String MIN_SIZE = "min_size";
		public static final String MAX_SIZE = "max_size";
		public static final String MIN_BRIGHTNESS = "min_brightness";
		public static final String MAX_BRIGHTNESS = "max_brightness";
		public static final String WEIGHT = "weight";
		
		private Color.IntRGB rgb;

		private float minSize;
		private float maxSize;

		private short minBrightness;
		private short maxBrightness;
		
		public int weight;
		
		public static final Codec<StarType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Color.IntRGB.CODEC.fieldOf("rgb").forGetter(StarType::getRGB),
				
				Codec.FLOAT.fieldOf("min_size").forGetter(starType -> starType.minSize),
				Codec.FLOAT.fieldOf("max_size").forGetter(starType -> starType.maxSize),
				
				Codec.SHORT.fieldOf("min_brightness").forGetter(starType -> starType.minBrightness),
				Codec.SHORT.fieldOf("max_brightness").forGetter(starType -> starType.maxBrightness),
				
				Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight").forGetter(StarType::getWeight)
				).apply(instance, StarType::new));
		
		public StarType() {}
		
		public StarType(Color.IntRGB rgb, float minSize, float maxSize, short minBrightness, short maxBrightness, int weight)
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
