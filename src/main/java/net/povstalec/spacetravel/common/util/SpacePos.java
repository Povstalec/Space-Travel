package net.povstalec.spacetravel.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.spacetravel.client.RenderCenter;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class SpacePos implements INBTSerializable<CompoundTag>
{
	public static final String X = "x";
	public static final String Y = "y";
	public static final String Z = "z";
	
	public static final double LY_TO_KM = 9_460_730_472_581.2;
	public static final double MAX_KM_VALUE = LY_TO_KM / 2;
	
	public static final double LIGHT_SPEED = 299_792.458;
	
	public static final SpacePos NULL_COORDS = new SpacePos();
    
    public static final Codec<SpacePos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		SpaceDist.CODEC.fieldOf(X).forGetter(SpacePos::x),
    		SpaceDist.CODEC.fieldOf(Y).forGetter(SpacePos::y),
    		SpaceDist.CODEC.fieldOf(Z).forGetter(SpacePos::z)
			).apply(instance, SpacePos::new));
	
	private SpaceDist x;
	private SpaceDist y;
	private SpaceDist z;
	
	public SpacePos(SpaceDist x, SpaceDist y, SpaceDist z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public SpacePos(long lyX, long lyY, long lyZ, double kmX, double kmY, double kmZ)
	{
		this(new SpaceDist(lyX, kmX), new SpaceDist(lyY, kmY), new SpaceDist(lyZ, kmZ));
	}
	
	public SpacePos(long x, long y, long z)
	{
		this(x, y, z, 0, 0, 0);
	}
	
	public SpacePos(double x, double y, double z)
	{
		this(0, 0, 0, x, y, z);
	}
	
	public SpacePos(Vector3f vector)
	{
		this(0, 0, 0, vector.x, vector.y, vector.z);
	}
	
	public SpacePos()
	{
		this(0, 0, 0, 0, 0, 0);
	}
	
	//============================================================================================
	//************************************Relative coordinates************************************
	//============================================================================================
	
	/**
	 * @param other The other coordinates that are compared to these coordinates
	 * @return Returns squared distance between two coordinate values (mainly for use in checks, since square root operation is costly)
	 */
	public double distanceSquared(SpacePos other)
	{
		double xDistance = this.x.sub(other.x).toKm();
		double yDistance = this.y.sub(other.y).toKm();
		double zDistance = this.z.sub(other.z).toKm();
		
		return xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;
	}
	
	/**
	 * @param other The other coordinates that are compared to these coordinates
	 * @return Returns distance between two coordinate values
	 */
	public double distance(SpacePos other)
	{
		return Math.sqrt(distanceSquared(other));
	}
	
	public double distanceToCenterSquared()
	{
		return distanceSquared(NULL_COORDS);
	}
	
	public double distanceToCenter()
	{
		return distance(NULL_COORDS);
	}
	
	public static Quaternionf getQuaternionf(ClientLevel level, RenderCenter renderCenter, float partialTicks)
	{
		Quaternionf q = new Quaternionf();
		// Inverting so that we can view the world through the relative rotation of our view center
		renderCenter.getObjectAxisRotation().quaternionf().invert(q);
		
		return q;
	}
	
	/**
	 * @param viewCenter The coordinates this object is viewed from
	 * @param r The radius of the sphere onto which the sky position is projected
	 * @return Returns the sky position at which the coordinates of this would appear on the sky when viewed from the viewCenter
	 */
	public SphericalCoords skyPosition(SpacePos viewCenter, float radius)
	{
		return new SphericalCoords(new Vector3d(this.x.sub(viewCenter.x).toKm(), this.y.sub(viewCenter.y).toKm(), this.z.sub(viewCenter.z).toKm()), radius);
	}
	
	/**
	 * @param viewCenter The coordinates this object is viewed from
	 * @return Returns the sky position at which the coordinates of this would appear on the sky when viewed from the viewCenter
	 */
	public SphericalCoords skyPosition(SpacePos viewCenter)
	{
		return new SphericalCoords(new Vector3d(this.x.sub(viewCenter.x).toKm(), this.y.sub(viewCenter.y).toKm(), this.z.sub(viewCenter.z).toKm()));
	}
	
	public SpacePos add(SpacePos other)
	{
		return new SpacePos(this.x.add(other.x), this.y.add(other.y), this.z.add(other.z));
	}
	
	public SpacePos add(Vector3f vector)
	{
		return new SpacePos(this.x.add(vector.x), this.y.add(vector.y), this.z.add(vector.z));
	}
	
	public SpacePos sub(SpacePos other)
	{
		return new SpacePos(this.x.sub(other.x), this.y.sub(other.y), this.z.sub(other.z));
	}
	
	public SpacePos sub(Vector3f vector)
	{
		return new SpacePos(this.x.sub(vector.x), this.y.sub(vector.y), this.z.sub(vector.z));
	}
	
	//============================================================================================
	//************************************Getters and Setters*************************************
	//============================================================================================
	
	public SpaceDist x()
	{
		return x;
	}
	
	public SpaceDist y()
	{
		return y;
	}
	
	public SpaceDist z()
	{
		return z;
	}
	
	public SpacePos copy()
	{
		return new SpacePos(x.copy(), y.copy(), z.copy());
	}
	
	@Override
	public String toString()
	{
		return "( x: " + x.toString() + ", y: " + y.toString() + ", z: " + z.toString() + " )";
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();
		tag.put(X, x.serializeNBT());
		tag.put(Y, y.serializeNBT());
		tag.put(Z, z.serializeNBT());
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		x.deserializeNBT(tag.getCompound(X));;
		y.deserializeNBT(tag.getCompound(Y));;
		z.deserializeNBT(tag.getCompound(Z));;
	}
	
	public void writeToBuffer(FriendlyByteBuf buffer)
	{
		x.writeToBuffer(buffer);
		y.writeToBuffer(buffer);
		z.writeToBuffer(buffer);
	}
	
	public static SpacePos readFromBuffer(FriendlyByteBuf buffer)
	{
		return new SpacePos(SpaceDist.readFromBuffer(buffer), SpaceDist.readFromBuffer(buffer), SpaceDist.readFromBuffer(buffer));
	}
	
	
	
	public static class SpaceDist implements INBTSerializable<CompoundTag>
	{
		public static final String LY = "ly";
		public static final String KM = "km";
		
		private long ly; // Light Years
		private double km; // Kilometers
		
		public static final Codec<SpaceDist> CODEC = RecordCodecBuilder.create(instance -> instance.group(
	    		// Coordinates in Light Years
				Codec.LONG.optionalFieldOf(LY, 0L).forGetter(SpaceDist::ly),
				// Coordinates in Kilometers
				Codec.DOUBLE.optionalFieldOf(KM, 0D).forGetter(SpaceDist::km)
				).apply(instance, SpaceDist::new));
		
		public SpaceDist(long lightYears, double kilometers)
		{
			this.ly = lightYears;
			this.km = kilometers;
			
			handleKmOverflow();
		}
		
		public SpaceDist(long lightYears)
		{
			this(lightYears, 0);
		}
		
		public SpaceDist(double kilometers)
		{
			this(0, kilometers);
		}
		
		private void handleKmOverflow()
		{
			if(this.km >= LY_TO_KM || this.km <= -LY_TO_KM)
			{
				long additionalLightYears = kmToLy(this.km );
				double subKm = this.km - lyToKm(additionalLightYears);
				
				this.ly += additionalLightYears;
				this.km -= subKm;
			}
		}
		
		public long ly()
		{
			return ly;
		}
		
		public double km()
		{
			return km;
		}
		
		
		
		public static long kmToLy(double km)
		{
			return (long) (km / LY_TO_KM);
		}
		
		public static double lyToKm(long ly)
		{
			return LY_TO_KM * ly;
		}
		
		
		
		public double toKm()
		{
			return km + lyToKm(ly);
		}
		
		public double toLy()
		{
			return ly + km / LY_TO_KM;
		}
		
		public SpaceDist add(SpaceDist other)
		{
			return new SpaceDist(this.ly + other.ly, this.km + other.km);
		}
		
		public SpaceDist add(double value)
		{
			return new SpaceDist(this.ly, this.km + value);
		}
		
		public SpaceDist sub(SpaceDist other)
		{
			return new SpaceDist(this.ly - other.ly, this.km - other.km);
		}
		
		public SpaceDist sub(double value)
		{
			return new SpaceDist(this.ly, this.km - value);
		}
		
		public SpaceDist mul(double value, boolean roundDown)
		{
			double result = this.ly * value;
			long ly = (long) result;
			
			return new SpaceDist((long) ly, roundDown ? 0 : result - ly);
		}
		
		public SpaceDist copy()
		{
			return new SpaceDist(ly, km);
		}
		
		@Override
		public String toString()
		{
			return "[ly: " + ly + ", km: " + km + "]";
		}
		
		//============================================================================================
		//*************************************Saving and Loading*************************************
		//============================================================================================
		
		@Override
		public CompoundTag serializeNBT()
		{
			CompoundTag tag = new CompoundTag();
			tag.putLong(LY, ly);
			tag.putDouble(KM, km);
			
			return tag;
		}
		
		@Override
		public void deserializeNBT(CompoundTag tag)
		{
			ly = tag.getLong(LY);
			km = tag.getDouble(KM);
		}
		
		public void writeToBuffer(FriendlyByteBuf buffer)
		{
			buffer.writeLong(ly);
			buffer.writeDouble(km);
		}
		
		public static SpaceDist readFromBuffer(FriendlyByteBuf buffer)
		{
			return new SpaceDist(buffer.readLong(), buffer.readDouble());
		}
	}
}
