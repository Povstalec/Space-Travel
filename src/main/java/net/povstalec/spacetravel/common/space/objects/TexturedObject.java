package net.povstalec.spacetravel.common.space.objects;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TexturedObject extends SpaceObject
{
	
	public static final ResourceLocation TEXTURED_OBJECT_LOCATION = new ResourceLocation(SpaceTravel.MODID, "textured_object");
	public static final ResourceKey<Registry<SpaceObject>> REGISTRY_KEY = ResourceKey.createRegistryKey(TEXTURED_OBJECT_LOCATION);
	public static final Codec<ResourceKey<SpaceObject>> RESOURCE_KEY_CODEC = ResourceKey.codec(REGISTRY_KEY);
	
	private ArrayList<TextureLayer> textureLayers = new ArrayList<TextureLayer>();
	
	public TexturedObject() {}
	public TexturedObject(ResourceLocation objectType, Optional<String> parentName,Either<SpaceCoords, StellarCoordinates.Equatorial> coords,
					 AxisRotation axisRotation, List<TextureLayer> textureLayers)
	{
		super(objectType, parentName, coords, axisRotation);
		
		this.textureLayers = new ArrayList<TextureLayer>(textureLayers);
	}
	
	@Override
	public boolean isInitialized()
	{
		return super.isInitialized() && textureLayers != null;
	}
	
	public ArrayList<TextureLayer> getTextureLayers()
	{
		return textureLayers;
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = super.serializeNBT();
		
		// Serialize Texture Layers
		CompoundTag textureLayerTag = new CompoundTag();
		int i = 0;
		for(TextureLayer textureLayer : textureLayers)
		{
			textureLayerTag.put(String.valueOf(i), textureLayer.serialize());
			i++;
		}
		tag.put(TEXTURE_LAYERS, textureLayerTag);
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		super.deserializeNBT(tag);
		
		// Deserialize Texture Layers
		CompoundTag textureLayerTag = tag.getCompound(TEXTURE_LAYERS);
		for(int i = 0; i < textureLayerTag.size(); i++)
		{
			textureLayers.add(TextureLayer.deserialize(textureLayerTag.getCompound(String.valueOf(i))));
		}
	}
}
