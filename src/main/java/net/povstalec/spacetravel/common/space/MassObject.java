package net.povstalec.spacetravel.common.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.Random;

public interface MassObject
{
	public static final String MASS = "mass";
	
	@Nullable
	Mass mass();
	
	public static class Mass implements INBTSerializable<CompoundTag>
	{
		public static final String EARTH_MASSES = "earth_masses";
		public static final String SOLAR_MASSES = "solar_masses";
		
		public static final double SOLAR_TO_EARTH_MASS = 333000;
		
		double mass;
		
		public static final Codec<Mass> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.doubleRange(0, Double.MAX_VALUE).optionalFieldOf(SOLAR_MASSES, 0D).forGetter(mass -> mass.mass / SOLAR_TO_EARTH_MASS),
				Codec.doubleRange(0, Double.MAX_VALUE).optionalFieldOf(EARTH_MASSES, 0D).forGetter(mass -> mass.mass)
		).apply(instance, Mass::new));
		
		public Mass() {}
		
		public Mass(double solarMasses, double earthMasses)
		{
			this.mass = SOLAR_TO_EARTH_MASS * solarMasses + earthMasses;
		}
		
		public double earthMasses()
		{
			return mass;
		}
		
		public double solarMasses()
		{
			return mass / SOLAR_TO_EARTH_MASS;
		}
		
		//============================================================================================
		//*************************************Saving and Loading*************************************
		//============================================================================================
		
		@Override
		public CompoundTag serializeNBT()
		{
			CompoundTag tag = new CompoundTag();
			
			tag.putDouble(MASS, mass);
			
			return tag;
		}
		
		@Override
		public void deserializeNBT(CompoundTag tag)
		{
			this.mass = tag.getDouble(MASS);
		}
		
		
		
		public static Mass randomEarthMass(Random random, double min, double max)
		{
			return new Mass(0D, random.nextDouble(min, max));
		}
		
		public static Mass randomSolarMass(Random random, double min, double max)
		{
			return new Mass(random.nextDouble(min, max), 0D);
		}
	}
}
