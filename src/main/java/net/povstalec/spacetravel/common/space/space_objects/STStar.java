package net.povstalec.spacetravel.common.space.space_objects;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.common.space.MassObject;
import net.povstalec.stellarview.api.common.space_objects.OrbitingObject;
import net.povstalec.stellarview.api.common.space_objects.TexturedObject;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Star;
import net.povstalec.stellarview.common.util.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class STStar extends Star implements MassObject
{
	@Nullable
	protected Mass mass;
	
	public static final Codec<STStar> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf(PARENT_LOCATION).forGetter(STStar::getParentLocation),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf(COORDS).forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf(AXIS_ROTATION).forGetter(STStar::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf(ORBIT_INFO).forGetter(star -> Optional.ofNullable(star.orbitInfo())),
			TextureLayer.CODEC.listOf().fieldOf(TEXTURE_LAYERS).forGetter(STStar::getTextureLayers),
			
			FadeOutHandler.CODEC.optionalFieldOf(FADE_OUT_HANDLER, FadeOutHandler.DEFAULT_STAR_HANDLER).forGetter(STStar::getFadeOutHandler),
			
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf(MIN_STAR_SIZE, MIN_SIZE).forGetter(STStar::getMinStarSize),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf(MAX_STAR_ALPHA, MAX_ALPHA).forGetter(STStar::getMaxStarAlpha),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf(MIN_STAR_ALPHA, MIN_ALPHA).forGetter(STStar::getMinStarAlpha),
			
			SupernovaInfo.CODEC.optionalFieldOf(SUPERNOVA_INFO).forGetter(star -> Optional.ofNullable(star.supernovaInfo())),
			
			Mass.CODEC.optionalFieldOf(MASS).forGetter(star -> Optional.ofNullable(star.mass))
	).apply(instance, STStar::new));
	
	public STStar() {}
	
	public STStar(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation, Optional<OrbitingObject.OrbitInfo> orbitInfo,
				  List<TextureLayer> textureLayers, TexturedObject.FadeOutHandler fadeOutHandler, float minStarSize, float maxStarAlpha, float minStarAlpha, Optional<SupernovaInfo> supernovaInfo,
				  Optional<Mass> mass)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler, minStarSize, maxStarAlpha, minStarAlpha, supernovaInfo);
		
		this.mass = mass.orElse(null);
	}
	
	@Nullable
	@Override
	public Mass mass()
	{
		return mass;
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = super.serializeNBT();
		
		if(mass != null)
			tag.put(MASS, mass.serializeNBT());
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		super.deserializeNBT(tag);
		
		if(tag.contains(MASS))
		{
			this.mass = new Mass();
			this.mass.deserializeNBT(tag.getCompound(MASS));
		}
	}
}
