package net.povstalec.spacetravel.common.space.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Random;
import java.util.function.Function;

public class SpaceTravelParameters
{
	public static final String MIN = "min";
	public static final String MAX = "max";
	
	public static class IntRange
	{
		public static final Codec<IntRange> DUST_CLOUD_RANGE_CODEC = IntRange.codec(0, 4000);
		public static final Codec<IntRange> STAR_RANGE_CODEC = IntRange.codec(0, 30000);
		public static final Codec<IntRange> DIAMETER_RANGE_CODEC = IntRange.codec(0, Integer.MAX_VALUE);
		public static final Codec<IntRange> ARM_NUMBER_RANGE_CODEC = IntRange.codec(0, 8);
		
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
		
		public static Codec<IntRange> codec(int min, int max)
		{
			return RecordCodecBuilder.create(instance -> instance.group(
					Codec.intRange(min, max).fieldOf(MIN).forGetter(range -> range.min),
					Codec.intRange(min, max).fieldOf(MAX).forGetter(range -> range.max)
			).apply(instance, IntRange::new));
		}
	}
	
	
	
	public static class LongRange
	{
		private static final PrimitiveCodec<Long> LONG = new PrimitiveCodec<Long>()
		{
			public <T> DataResult<Long> read(DynamicOps<T> ops, T input)
			{
				return ops.getNumberValue(input).map(Number::longValue);
			}
			
			public <T> T write(DynamicOps<T> ops, Long value)
			{
				return ops.createLong(value);
			}
			
			public String toString()
			{
				return "Long";
			}
		};
		
		public long min;
		public long max;
		
		public LongRange(long min, long max)
		{
			if(min > max)
				throw new IllegalArgumentException("min must be less than or equal to max");
			
			this.min = min;
			this.max = max;
		}
		
		public long nextLong(Random random)
		{
			if(min == max)
				return min;
			
			return random.nextLong(min, max);
		}
		
		public static Codec<LongRange> codec(long min, long max)
		{
			return RecordCodecBuilder.create(instance -> instance.group(
					longRange(min, max).fieldOf(MIN).forGetter(range -> range.min),
					longRange(min, max).fieldOf(MAX).forGetter(range -> range.max)
			).apply(instance, LongRange::new));
		}
		
		private static Codec<Long> longRange(long minInclusive, long maxInclusive)
		{
			Function<Long, DataResult<Long>> checker = checkRange(minInclusive, maxInclusive);
			return LONG.flatXmap(checker, checker);
		}
		
		private static <N extends Number & Comparable<N>> Function<N, DataResult<N>> checkRange(N minInclusive, N maxInclusive)
		{
			return (value) ->
			{
				return ((Comparable)value).compareTo(minInclusive) >= 0 && ((Comparable)value).compareTo(maxInclusive) <= 0 ? DataResult.success(value) : DataResult.error(() ->
				{
					return "Value " + value + " outside of range [" + minInclusive + ":" + maxInclusive + "]";
				}, value);
			};
		}
	}
	
	
	
	public static class FloatRange
	{
		public static final Codec<FloatRange> FULL_RANGE_CODEC = FloatRange.codec(Float.MIN_VALUE, Float.MAX_VALUE);
		public static final Codec<FloatRange> POSITIVE_RANGE_CODEC = FloatRange.codec(0F, Float.MAX_VALUE);
		public static final Codec<FloatRange> ANGLE_RANGE_CODEC = FloatRange.codec(0F, 360F);
		
		public float min;
		public float max;
		
		public FloatRange(float min, float max)
		{
			if(min > max)
				throw new IllegalArgumentException("min must be less than or equal to max");
			
			this.min = min;
			this.max = max;
		}
		
		public float nextFloat(Random random)
		{
			if(min == max)
				return min;
			
			return random.nextFloat(min, max);
		}
		
		public static Codec<FloatRange> codec(float min, float max)
		{
			return RecordCodecBuilder.create(instance -> instance.group(
					Codec.floatRange(min, max).fieldOf(MIN).forGetter(range -> range.min),
					Codec.floatRange(min, max).fieldOf(MAX).forGetter(range -> range.max)
			).apply(instance, FloatRange::new));
		}
	}
	
	
	
	public static class DoubleRange
	{
		public static final Codec<DoubleRange> FULL_RANGE_CODEC = DoubleRange.codec(Double.MIN_VALUE, Double.MAX_VALUE);
		public static final Codec<DoubleRange> POSITIVE_RANGE_CODEC = DoubleRange.codec(0D, Double.MAX_VALUE);
		public static final Codec<DoubleRange> ANGLE_RANGE_CODEC = DoubleRange.codec(0D, 360D);
		
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
		
		public static Codec<DoubleRange> codec(double min, double max)
		{
			return RecordCodecBuilder.create(instance -> instance.group(
					Codec.doubleRange(min, max).fieldOf(MIN).forGetter(range -> range.min),
					Codec.doubleRange(min, max).fieldOf(MAX).forGetter(range -> range.max)
			).apply(instance, DoubleRange::new));
		}
	}
}
