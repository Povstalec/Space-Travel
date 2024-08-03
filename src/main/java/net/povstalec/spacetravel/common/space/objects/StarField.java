package net.povstalec.spacetravel.common.space.objects;

import java.util.List;
import java.util.Optional;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.common.util.AxisRotation;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import net.povstalec.spacetravel.common.util.StarInfo;
import net.povstalec.spacetravel.common.util.TextureLayer;

public abstract class StarField extends SpaceObject
{
	public static final String SEED = "seed";
	public static final String DIAMETER = "diameter";
	public static final String STARS = "stars";
	
	protected StarInfo starInfo;
	
	protected long seed;
	
	protected int diameter;
	protected int stars;
	
	public StarField() {}
	
	public StarField(ResourceLocation objectType, Optional<String> parentName, SpaceCoords coords, AxisRotation axisRotation,
			List<TextureLayer> textureLayers, long seed, int diameter, int numberOfStars)
	{
		super(objectType, parentName, coords, axisRotation, textureLayers);
		
		this.seed = seed;

		this.diameter = diameter;
		this.stars = numberOfStars;
	}
	
	public long getSeed()
	{
		return seed;
	}
	
	public int getDiameter()
	{
		return diameter;
	}
	
	public int getStars()
	{
		return stars;
	}
	
	/**
	 * @param level The Level the Player is currently in
	 * @param camera Player Camera
	 * @param partialTicks
	 * @return
	 */
	public static float getStarBrightness(ClientLevel level, Camera camera, float partialTicks)
	{
		float rain = 1.0F - level.getRainLevel(partialTicks);
		float starBrightness = level.getStarBrightness(partialTicks) * rain;
		
		return starBrightness;
	}

	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = super.serializeNBT();
		
		tag.putLong(SEED, seed);
		
		tag.putInt(DIAMETER, diameter);
		tag.putInt(STARS, stars);
		
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		super.deserializeNBT(tag);
		
		seed = tag.getLong(SEED);
		
		diameter = tag.getInt(DIAMETER);
		stars = tag.getInt(STARS);
	}
}
