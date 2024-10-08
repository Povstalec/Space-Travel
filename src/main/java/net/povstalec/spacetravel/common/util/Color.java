package net.povstalec.spacetravel.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class Color
{
	public static final int MAX_INT_VALUE = 255;
	public static final int MIN_INT_VALUE = 0;
	
	public static final String RED = "red";
	public static final String GREEN = "green";
	public static final String BLUE = "blue";
	public static final String ALPHA = "alpha";
	
	private static void checkValue(int value)
	{
		if(value > MAX_INT_VALUE)
			throw(new IllegalArgumentException("Value may not be higher than 255"));
		else if(value < MIN_INT_VALUE)
			throw(new IllegalArgumentException("Value may not be lower than 0"));
	}
	
	
	
	public static class IntRGB implements INBTSerializable<CompoundTag>
	{
		public static final Codec<Color.IntRGB> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.intRange(MIN_INT_VALUE, MAX_INT_VALUE).fieldOf(RED).forGetter(Color.IntRGB::red),
				Codec.intRange(MIN_INT_VALUE, MAX_INT_VALUE).fieldOf(GREEN).forGetter(Color.IntRGB::green),
				Codec.intRange(MIN_INT_VALUE, MAX_INT_VALUE).fieldOf(BLUE).forGetter(Color.IntRGB::blue)
		).apply(instance, Color.IntRGB::new));
		protected int red;
		protected int green;
		protected int blue;
		
		public IntRGB() {}
		
		public IntRGB(int red, int green, int blue)
		{
			if(red > MAX_INT_VALUE || green > MAX_INT_VALUE || blue > MAX_INT_VALUE)
				throw(new IllegalArgumentException("No value may be higher than 255"));
			else if(red < MIN_INT_VALUE || green < MIN_INT_VALUE || blue < MIN_INT_VALUE)
				throw(new IllegalArgumentException("No value may be lower than 0"));
			
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
		
		public void setRed(int red)
		{
			checkValue(red);
			
			this.red = red;
		}
		
		public int red()
		{
			return red;
		}
		
		public void setGreen(int green)
		{
			checkValue(green);
			
			this.green = green;
		}
		
		public int green()
		{
			return green;
		}
		
		public void setBlue(int blue)
		{
			checkValue(blue);
			
			this.blue = blue;
		}
		
		public int blue()
		{
			return blue;
		}

		@Override
		public CompoundTag serializeNBT()
		{
			CompoundTag tag = new CompoundTag();
			
			tag.putInt(RED, red);
			tag.putInt(GREEN, green);
			tag.putInt(BLUE, blue);
			
			return tag;
		}

		@Override
		public void deserializeNBT(CompoundTag tag)
		{
			red = tag.getInt(RED);
			green = tag.getInt(GREEN);
			blue = tag.getInt(BLUE);
		}
	}
	
	public static class IntRGBA extends IntRGB
	{
	    public static final Codec<Color.IntRGBA> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.intRange(MIN_INT_VALUE, MAX_INT_VALUE).fieldOf(RED).forGetter(Color.IntRGBA::red),
				Codec.intRange(MIN_INT_VALUE, MAX_INT_VALUE).fieldOf(GREEN).forGetter(Color.IntRGBA::green),
				Codec.intRange(MIN_INT_VALUE, MAX_INT_VALUE).fieldOf(BLUE).forGetter(Color.IntRGBA::blue),
				Codec.intRange(MIN_INT_VALUE, MAX_INT_VALUE).optionalFieldOf(ALPHA, MAX_INT_VALUE).forGetter(Color.IntRGBA::alpha)
				).apply(instance, Color.IntRGBA::new));
	    
	    protected int alpha;
		
		public IntRGBA(int red, int green, int blue, int alpha)
		{
			super(red, green, blue);
			
			if(alpha > MAX_INT_VALUE)
				throw(new IllegalArgumentException("No value may be higher than 255"));
			else if(alpha < MIN_INT_VALUE)
				throw(new IllegalArgumentException("No value may be lower than 0"));
			
			this.alpha = alpha;
		}
		
		public IntRGBA(int red, int green, int blue)
		{
			this(red, green, blue, 255);
		}
		
		public void setAlpha(int alpha)
		{
			checkValue(alpha);
			
			this.alpha = alpha;
		}
		
		public int alpha()
		{
			return alpha;
		}

		@Override
		public CompoundTag serializeNBT()
		{
			CompoundTag tag = super.serializeNBT();
			
			tag.putInt(ALPHA, alpha);
			
			return tag;
		}

		@Override
		public void deserializeNBT(CompoundTag tag)
		{
			super.deserializeNBT(tag);
			
			alpha = tag.getInt(ALPHA);
		}
	}
}
