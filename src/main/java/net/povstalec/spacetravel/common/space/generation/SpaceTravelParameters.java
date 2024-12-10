package net.povstalec.spacetravel.common.space.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.povstalec.spacetravel.common.space.objects.StarField;

import java.util.Random;

public class SpaceTravelParameters
{
	public static final String MIN = "min";
	public static final String MAX = "max";
	
	
	
	public static final Codec<IntRange> DUST_CLOUD_RANGE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.intRange(0, 4000).fieldOf(MIN).forGetter(range -> range.min),
			Codec.intRange(0, 4000).fieldOf(MAX).forGetter(range -> range.max)
	).apply(instance, IntRange::new));
	
	public static final Codec<IntRange> STAR_RANGE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.intRange(0, 30000).fieldOf(MIN).forGetter(range -> range.min),
			Codec.intRange(0, 30000).fieldOf(MAX).forGetter(range -> range.max)
	).apply(instance, IntRange::new));
	
	public static final Codec<IntRange> DIAMETER_RANGE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.intRange(0, Integer.MAX_VALUE).fieldOf(MIN).forGetter(range -> range.min),
			Codec.intRange(0, Integer.MAX_VALUE).fieldOf(MAX).forGetter(range -> range.max)
	).apply(instance, IntRange::new));
	
	public static final Codec<IntRange> ARM_NUMBER_RANGE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.intRange(0, 8).fieldOf(MIN).forGetter(range -> range.min),
			Codec.intRange(0, 8).fieldOf(MAX).forGetter(range -> range.max)
	).apply(instance, IntRange::new));
	
	public static class IntRange
	{
		public int min;
		public int max;
		
		public IntRange(int min, int max)
		{
			if(min > max)
				throw new IllegalArgumentException("min must be less than or equal to max");
			
			this.min = min;
			this.max = max;
		}
		
		public int nextInt(Random random)
		{
			if(min == max)
				return min;
			
			return random.nextInt(min, max);
		}
	}
	
	
	
	public static final Codec<DoubleRange> DOUBLE_RANGE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.DOUBLE.fieldOf(MIN).forGetter(range -> range.min),
			Codec.DOUBLE.fieldOf(MAX).forGetter(range -> range.max)
	).apply(instance, DoubleRange::new));
	
	public static final Codec<DoubleRange> DOUBLE_POSITIVE_RANGE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.doubleRange(0, Double.MAX_VALUE).fieldOf(MIN).forGetter(range -> range.min),
			Codec.doubleRange(0, Double.MAX_VALUE).fieldOf(MAX).forGetter(range -> range.max)
	).apply(instance, DoubleRange::new));
	
	public static final Codec<DoubleRange> ANGLE_RANGE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.doubleRange(0, 360D).fieldOf(MIN).forGetter(range -> range.min),
			Codec.doubleRange(0, 360D).fieldOf(MAX).forGetter(range -> range.max)
	).apply(instance, DoubleRange::new));
	
	public static class DoubleRange
	{
		public double min;
		public double max;
		
		public DoubleRange(double min, double max)
		{
			if(min > max)
				throw new IllegalArgumentException("min must be less than or equal to max");
			
			this.min = min;
			this.max = max;
		}
		
		public double nextDouble(Random random)
		{
			if(min == max)
				return min;
			
			return random.nextDouble(min, max);
		}
	}
}
