package net.povstalec.spacetravel.common.space.objects;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.spacetravel.client.render.StarBuffer;
import net.povstalec.spacetravel.common.util.AxisRotation;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import net.povstalec.spacetravel.common.util.StarInfo;
import net.povstalec.spacetravel.common.util.TextureLayer;

public abstract class StarField extends SpaceObject
{
	protected StarInfo starInfo;
	
	protected final long seed;
	
	protected final int diameter;
	protected final int stars;
	
	public StarField(Optional<String> parentName, SpaceCoords coords, AxisRotation axisRotation,
			List<TextureLayer> textureLayers, long seed, int diameter, int numberOfStars)
	{
		super(parentName, coords, axisRotation, textureLayers);
		
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
}
