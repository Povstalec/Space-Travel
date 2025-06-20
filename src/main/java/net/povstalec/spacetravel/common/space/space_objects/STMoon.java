package net.povstalec.spacetravel.common.space.space_objects;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.capabilities.ViewObjectCapabilityProvider;
import net.povstalec.spacetravel.common.space.DimensionObject;
import net.povstalec.spacetravel.common.space.GenerationObject;
import net.povstalec.spacetravel.common.space.LoadableObject;
import net.povstalec.spacetravel.common.space.MassObject;
import net.povstalec.spacetravel.common.space.generation.parameters.WorldGenInfo;
import net.povstalec.spacetravel.common.util.DimensionUtil;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Moon;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Planet;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class STMoon extends Moon implements MassObject, GenerationObject, DimensionObject, LoadableObject
{
	public static final String WORLDGEN_INFO = "worldgen_info";
	
	protected boolean isLoaded = false;
	
	@Nullable
	protected ResourceLocation generationParameters;
	
	@Nullable
	protected Mass mass;
	@Nullable
	protected ResourceKey<Level> dimension;
	@Nullable
	protected WorldGenInfo worldGenInfo;
	
	public static final Codec<STMoon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf(PARENT_LOCATION).forGetter(STMoon::getParentLocation),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf(COORDS).forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf(AXIS_ROTATION).forGetter(STMoon::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf(ORBIT_INFO).forGetter(planet -> Optional.ofNullable(planet.orbitInfo())),
			TextureLayer.CODEC.listOf().fieldOf(TEXTURE_LAYERS).forGetter(STMoon::getTextureLayers),
			
			FadeOutHandler.CODEC.optionalFieldOf(FADE_OUT_HANDLER, FadeOutHandler.DEFAULT_PLANET_HANDLER).forGetter(STMoon::getFadeOutHandler),
			
			Mass.CODEC.optionalFieldOf(MASS).forGetter(planet -> Optional.ofNullable(planet.mass)),
			Level.RESOURCE_KEY_CODEC.optionalFieldOf(DIMENSION).forGetter(planet -> Optional.ofNullable(planet.dimension)),
			WorldGenInfo.CODEC.optionalFieldOf(WORLDGEN_INFO).forGetter(planet -> Optional.ofNullable(planet.worldGenInfo))
	).apply(instance, STMoon::new));
	
	public STMoon() {}
	
	public STMoon(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
				  Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler,
				  Optional<Mass> mass, Optional<ResourceKey<Level>> dimension, Optional<WorldGenInfo> worldGenInfo)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler, Optional.empty());
		
		this.mass = mass.orElse(null);
		this.dimension = dimension.orElse(null);
		this.worldGenInfo = worldGenInfo.orElse(null);
	}
	
	@Nullable
	@Override
	public Mass mass()
	{
		return mass;
	}
	
	@Nullable
	@Override
	public ResourceKey<Level> dimension()
	{
		return dimension;
	}
	
	@Nullable
	@Override
	public WorldGenInfo worldGenInfo()
	{
		return worldGenInfo;
	}
	
	//============================================================================================
	//*****************************************Generation*****************************************
	//============================================================================================
	
	@Nullable
	@Override
	public ResourceLocation getGenerationParameters()
	{
		return generationParameters;
	}
	
	@Override
	public void setGenerationParameters(ResourceLocation generationParameters)
	{
		this.generationParameters = generationParameters;
	}
	
	@Override
	public long generationSeed()
	{
		return this.getCoords().hashCode();
	}
	
	@Override
	public SpaceObject generationParent()
	{
		return this;
	}
	
	//============================================================================================
	//******************************************Dimension*****************************************
	//============================================================================================
	
	@Override
	public boolean hasSurface()
	{
		return dimension() != null || worldGenInfo() != null;
	}
	
	@Nullable
	@Override
	public ServerLevel generateWorld(MinecraftServer server)
	{
		if(worldGenInfo() == null)
			return null;
		
		if(dimension() == null)
			this.dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(SpaceTravel.MODID, UUID.randomUUID().toString()));
		
		ServerLevel level = DimensionUtil.createWorld(server, dimension(), worldGenInfo().biomes(), worldGenInfo().settings());
		
		load(server);
		return level;
	}
	
	@Nullable
	@Override
	public ServerLevel getLevel(MinecraftServer server, boolean generate)
	{
		if(!generate)
		{
			if(dimension() != null)
				return server.getLevel(dimension());
			else
				return null;
		}
		else
		{
			ServerLevel level = server.getLevel(dimension());
			if(level != null)
				return level;
			else
				return generateWorld(server);
		}
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public boolean isLoaded()
	{
		return this.isLoaded;
	}
	
	@Override
	public void load(MinecraftServer server)
	{
		if(dimension() == null)
			return;
		
		ServerLevel level = server.getLevel(dimension());
		if(level != null)
		{
			level.getCapability(ViewObjectCapabilityProvider.VIEW_OBJECT).ifPresent(cap ->
			{
				if(cap.viewObject() == null)
				{
					cap.setViewObject(this);
					this.isLoaded = true;
				}
			});
		}
	}
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = super.serializeNBT();
		
		if(mass != null)
			tag.put(MASS, mass.serializeNBT());
		
		if(generationParameters != null)
			tag.putString(GENERATION_PARAMETERS, generationParameters.toString());
		
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
		
		if(tag.contains(GENERATION_PARAMETERS))
			this.generationParameters = new ResourceLocation(tag.getString(GENERATION_PARAMETERS));
		
		if(tag.contains(DIMENSION))
			dimension = DimensionUtil.stringToDimension(tag.getString(DIMENSION));
	}
}
