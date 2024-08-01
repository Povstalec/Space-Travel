package net.povstalec.spacetravel.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.INBTSerializable;

public class AxisRotation implements INBTSerializable<CompoundTag>
{
	public static final String X_AXIS = "x_axis";
	public static final String Y_AXIS = "y_axis";
	public static final String Z_AXIS = "z_axis";
	
	private boolean inDegrees;
	
	public double xAxis;
	public double yAxis;
	public double zAxis;
	
	public static final Codec<AxisRotation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("in_degrees", true).forGetter(axisRotation -> axisRotation.inDegrees),
			Codec.DOUBLE.fieldOf(X_AXIS).forGetter(AxisRotation::xAxis),
			Codec.DOUBLE.fieldOf(Y_AXIS).forGetter(AxisRotation::yAxis),
			Codec.DOUBLE.fieldOf(Z_AXIS).forGetter(AxisRotation::zAxis)
			).apply(instance, AxisRotation::new));
	
	public AxisRotation(boolean inDegrees, double xAxis, double yAxis, double zAxis)
	{
		this.inDegrees = inDegrees;
		
		if(inDegrees)
		{
			this.xAxis = Math.toRadians(xAxis);
			this.yAxis = Math.toRadians(yAxis);
			this.zAxis = Math.toRadians(zAxis);
		}
		else
		{
			this.xAxis = xAxis;
			this.yAxis = yAxis;
			this.zAxis = zAxis;
		}
	}
	
	/**
	 * 
	 * @param xAxis Rotation around the X-axis (in degrees)
	 * @param yAxis Rotation around the Y-axis (in degrees)
	 * @param zAxis Rotation around the Z-axis (in degrees)
	 */
	public AxisRotation(double xAxis, double yAxis, double zAxis)
	{
		this(true, xAxis, yAxis, zAxis);
	}
	public AxisRotation()
	{
		this(false, 0, 0, 0);
	}
	
	/**
	 * @return Rotation around the X-Axis (in radians)
	 */
	public double xAxis()
	{
		return xAxis;
	}
	
	/**
	 * @return Rotation around the Y-Axis (in radians)
	 */
	public double yAxis()
	{
		return yAxis;
	}
	
	public AxisRotation add(AxisRotation other)
	{
		return new AxisRotation(this.xAxis + other.xAxis, this.yAxis + other.yAxis, this.zAxis + other.zAxis);
	}
	
	public AxisRotation add(double xRot, double yRot, double zRot)
	{
		return new AxisRotation(this.xAxis + xRot, this.yAxis + yRot, this.zAxis + zRot);
	}
	
	/**
	 * @return Rotation around the Z-Axis (in radians)
	 */
	public double zAxis()
	{
		return zAxis;
	}
	
	public AxisRotation copy()
	{
		return new AxisRotation(false, xAxis, yAxis, zAxis);
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();
		
		tag.putDouble(X_AXIS, xAxis);
		tag.putDouble(Y_AXIS, yAxis);
		tag.putDouble(Z_AXIS, zAxis);
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		inDegrees = false;
		xAxis = tag.getDouble(X_AXIS);
		yAxis = tag.getDouble(Y_AXIS);
		zAxis = tag.getDouble(Z_AXIS);
	}
	
	public void writeToBuffer(FriendlyByteBuf buffer)
	{
		buffer.writeDouble(xAxis);
		buffer.writeDouble(yAxis);
		buffer.writeDouble(zAxis);
	}
	
	public static AxisRotation readFromBuffer(FriendlyByteBuf buffer)
	{
		return new AxisRotation(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
	}
}
