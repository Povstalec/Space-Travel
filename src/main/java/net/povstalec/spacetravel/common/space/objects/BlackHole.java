package net.povstalec.spacetravel.common.space.objects;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.util.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class BlackHole extends StarLike
{
	public static final String LENSING_INTENSITY = "lensing_intensity";
	
	public static final ResourceLocation BLACK_HOLE_LOCATION = new ResourceLocation(SpaceTravel.MODID, "black_hole");
	public static final ResourceKey<Registry<BlackHole>> REGISTRY_KEY = ResourceKey.createRegistryKey(BLACK_HOLE_LOCATION);
	
	protected float lensingIntensity;
	
	public static final Codec<BlackHole> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(BlackHole::getParentLocation),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getSpaceCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(BlackHole::getAxisRotation),
			
			FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", FadeOutHandler.DEFAULT_STAR_HANDLER).forGetter(BlackHole::getFadeOutHandler),
			
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(BlackHole::getTextureLayers),
			
			OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(BlackHole::getOrbitInfo),
			
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("min_black_hole_size", MIN_SIZE).forGetter(BlackHole::getMinStarSize),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("max_black_hole_alpha", MAX_ALPHA).forGetter(BlackHole::getMaxStarAlpha),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("min_black_hole_alpha", MIN_ALPHA).forGetter(BlackHole::getMinStarAlpha),
			
			Codec.floatRange(1F, Float.MAX_VALUE).optionalFieldOf(LENSING_INTENSITY, 2F).forGetter(BlackHole::getLensingIntensity)
			).apply(instance, BlackHole::new));
	
	public BlackHole() {};
	
	public BlackHole(Optional<ResourceLocation> parentLocation, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
					 FadeOutHandler fadeOutHandler, List<TextureLayer> textureLayers, Optional<OrbitInfo> orbitInfo,
					 float minStarSize, float maxStarAlpha, float minStarAlpha, float lensingIntensity)
	{
		super(BLACK_HOLE_LOCATION, parentLocation, coords, axisRotation, fadeOutHandler, textureLayers, orbitInfo, minStarSize, maxStarAlpha, minStarAlpha);
		
		this.lensingIntensity = lensingIntensity;
	}
	
	public float getLensingIntensity()
	{
		return lensingIntensity;
	}
	
	public float getLensingIntensity(double distance)
	{
		float lensingIntensity = getLensingIntensity();
		
		lensingIntensity -= (float) (distance / (10000 * SpaceCoords.LY_TO_KM)); // 5000000000D
		
		return lensingIntensity;
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = super.serializeNBT();
		
		tag.putFloat(LENSING_INTENSITY, lensingIntensity);
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		super.deserializeNBT(tag);
		
		lensingIntensity = tag.getFloat(LENSING_INTENSITY);
	}
}
