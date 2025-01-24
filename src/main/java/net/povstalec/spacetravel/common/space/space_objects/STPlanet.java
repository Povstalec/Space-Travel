package net.povstalec.spacetravel.common.space.space_objects;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.povstalec.spacetravel.common.space.DimensionObject;
import net.povstalec.spacetravel.common.space.MassObject;
import net.povstalec.spacetravel.common.util.DimensionUtil;
import net.povstalec.stellarview.api.common.space_objects.OrbitingObject;
import net.povstalec.stellarview.api.common.space_objects.TexturedObject;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Planet;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class STPlanet extends Planet implements MassObject, DimensionObject
{
	@Nullable
	protected Mass mass;
	@Nullable
	protected ResourceKey<Level> dimension;
	
	public static final Codec<STPlanet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf(PARENT_LOCATION).forGetter(STPlanet::getParentLocation),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf(COORDS).forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf(AXIS_ROTATION).forGetter(STPlanet::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf(ORBIT_INFO).forGetter(planet -> Optional.ofNullable(planet.orbitInfo())),
			TextureLayer.CODEC.listOf().fieldOf(TEXTURE_LAYERS).forGetter(STPlanet::getTextureLayers),
			
			FadeOutHandler.CODEC.optionalFieldOf(FADE_OUT_HANDLER, FadeOutHandler.DEFAULT_PLANET_HANDLER).forGetter(STPlanet::getFadeOutHandler),
			
			Mass.CODEC.optionalFieldOf(MASS).forGetter(planet -> Optional.ofNullable(planet.mass)),
			Level.RESOURCE_KEY_CODEC.optionalFieldOf(DIMENSION).forGetter(planet -> Optional.ofNullable(planet.dimension))
	).apply(instance, STPlanet::new));
	
	public STPlanet() {}
	
	public STPlanet(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
					Optional<OrbitingObject.OrbitInfo> orbitInfo,List<TextureLayer> textureLayers, TexturedObject.FadeOutHandler fadeOutHandler,
					Optional<Mass> mass, Optional<ResourceKey<Level>> dimension)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler);
		
		this.mass = mass.orElse(null);
		this.dimension = dimension.orElse(null);
	}
	
	@Nullable
	@Override
	public Mass mass()
	{
		return mass;
	}
	
	@Override
	public ResourceKey<Level> dimension()
	{
		return dimension;
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
		
		if(dimension != null)
			tag.putString(DIMENSION, dimension().location().toString());
		
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
		
		if(tag.contains(DIMENSION))
			dimension = DimensionUtil.stringToDimension(tag.getString(DIMENSION));
	}
}
