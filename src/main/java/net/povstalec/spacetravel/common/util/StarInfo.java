package net.povstalec.spacetravel.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.space.objects.StarField;
import net.povstalec.spacetravel.common.space.objects.StarLike;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class StarInfo implements INBTSerializable<CompoundTag>
{
	public static final ResourceLocation DEFAULT_STAR_TEXTURE = new ResourceLocation(SpaceTravel.MODID,"textures/environment/star.png");
	
	public static final String STAR_TEXTURE = "star_texture";
	public static final String LOD1_TYPES = "lod1_types";
	public static final String LOD2_TYPES = "lod2_types";
	public static final String LOD3_TYPES = "lod3_types";
	public static final String LOD1_WEIGHT = "lod1_weight";
	public static final String LOD2_WEIGHT = "lod2_weight";
	public static final String LOD3_WEIGHT = "lod3_weight";
	
	protected ResourceLocation starTexture;
	
	private ArrayList<StarLike.StarType> lod1Types;
	private ArrayList<StarLike.StarType> lod2Types;
	private ArrayList<StarLike.StarType> lod3Types;
	private int lod1Weight = 0;
	private int lod2Weight = 0;
	private int lod3Weight = 0;
	
	public static final StarLike.StarType O_CLASS = new StarLike.StarType(new Color.IntRGB(207, 207, 255), 0.25F, 0.35F, (short) 255, (short) 255, 10000000L, 1);
	public static final StarLike.StarType B_CLASS = new StarLike.StarType(new Color.IntRGB(223, 223, 255), 0.20F, 0.30F, (short) 225, (short) 255, 8000000L, 1);
	public static final StarLike.StarType A_CLASS = new StarLike.StarType(new Color.IntRGB(239, 239, 255), 0.15F, 0.25F, (short) 210, (short) 240, 6000000L, 1);
	public static final StarLike.StarType F_CLASS = new StarLike.StarType(new Color.IntRGB(255, 255, 255), 0.15F, 0.25F, (short) 190, (short) 230, 5500000L, 3);
	public static final StarLike.StarType G_CLASS = new StarLike.StarType(new Color.IntRGB(255, 255, 223), 0.10F, 0.25F, (short) 180, (short) 210, 5000000L, 7);
	public static final StarLike.StarType K_CLASS = new StarLike.StarType(new Color.IntRGB(255, 239, 223), 0.10F, 0.25F, (short) 120, (short) 200, 4500000L, 12);
	public static final StarLike.StarType M_CLASS = new StarLike.StarType(new Color.IntRGB(255, 223, 223), 0.10F, 0.25F, (short) 100, (short) 150, 4000000L, 74);
	public static final List<StarLike.StarType> DEFAULT_STARS = Arrays.asList(O_CLASS, B_CLASS, A_CLASS, F_CLASS, G_CLASS, K_CLASS, M_CLASS);
	public static final StarInfo DEFAULT_STAR_INFO = new StarInfo(DEFAULT_STAR_TEXTURE, DEFAULT_STARS);
	
	public static final Codec<StarInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("star_texture", DEFAULT_STAR_TEXTURE).forGetter(StarInfo::getStarTexture),
			StarLike.StarType.CODEC.listOf().fieldOf("star_types").forGetter(starInfo -> new ArrayList<StarLike.StarType>())
	).apply(instance, StarInfo::new));
	
	public StarInfo() {}
	
	public StarInfo(ResourceLocation starTexture, List<StarLike.StarType> starTypes)
	{
		this.starTexture = starTexture;
		
		for(StarLike.StarType starType : starTypes)
		{
			switch(StarField.LevelOfDetail.fromDistance(starType.getMaxVisibleDistance()))
			{
				case LOD1:
					if(lod1Types == null)
						this.lod1Types = new ArrayList<StarLike.StarType>(starTypes);
					this.lod1Types.add(starType);
					this.lod1Weight += starType.getWeight();
					break;
				case LOD2:
					if(lod2Types == null)
						this.lod2Types = new ArrayList<StarLike.StarType>(starTypes);
					this.lod2Types.add(starType);
					this.lod2Weight += starType.getWeight();
					break;
				default:
					if(lod3Types == null)
						this.lod3Types = new ArrayList<StarLike.StarType>(starTypes);
					this.lod3Types.add(starType);
					this.lod3Weight += starType.getWeight();
					break;
			}
		}
	}
	
	public ResourceLocation getStarTexture()
	{
		return starTexture;
	}
	
	private StarLike.StarType randomStarType(ArrayList<StarLike.StarType> lodTypes, int totalWeight, Random random)
	{
		if(lodTypes == null || lodTypes.isEmpty())
			return F_CLASS;
		
		int i = 0;
		
		for(int weight = random.nextInt(0, totalWeight); i < lodTypes.size() - 1; i++)
		{
			weight -= lodTypes.get(i).getWeight();
			
			if(weight <= 0)
				break;
		}
		
		return lodTypes.get(i);
	}
	
	public StarLike.StarType randomLOD1StarType(Random random)
	{
		return randomStarType(lod1Types, lod1Weight, random);
	}
	
	public StarLike.StarType randomLOD2StarType(Random random)
	{
		return randomStarType(lod2Types, lod2Weight, random);
	}
	
	public StarLike.StarType randomLOD3StarType(Random random)
	{
		return randomStarType(lod3Types, lod3Weight, random);
	}
	
	public int totalWeight()
	{
		return lod1Weight + lod2Weight + lod3Weight;
	}
	
	public int lod1Weight()
	{
		return lod1Weight;
	}
	
	public int lod2Weight()
	{
		return lod2Weight;
	}
	
	public int lod3Weight()
	{
		return lod3Weight;
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	private static CompoundTag serializeLODTypes(ArrayList<StarLike.StarType> lodTypes)
	{
		CompoundTag starTypesTag = new CompoundTag();
		for(int i = 0; i < lodTypes.size(); i++)
		{
			starTypesTag.put("star_type_" + i, lodTypes.get(i).serializeNBT());
		}
		
		return starTypesTag;
	}
	
	private static ArrayList<StarLike.StarType> getLODTypes(CompoundTag tag, String key)
	{
		ArrayList<StarLike.StarType> lodTypes;
		if(tag.contains(key))
		{
			lodTypes = new ArrayList<StarLike.StarType>();
			CompoundTag starTypesTag = tag.getCompound(key);
			for(int i = 0; i < starTypesTag.size(); i++)
			{
				StarLike.StarType starType = new StarLike.StarType();
				starType.deserializeNBT(starTypesTag.getCompound("star_type_" + i));
				lodTypes.add(starType);
			}
		}
		else
			lodTypes = null;
		
		return lodTypes;
	}
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();
		
		tag.putString(STAR_TEXTURE, starTexture.toString());
		
		if(this.lod1Types != null)
			tag.put(LOD1_TYPES, serializeLODTypes(this.lod1Types));
		if(this.lod2Types != null)
			tag.put(LOD2_TYPES, serializeLODTypes(this.lod2Types));
		if(this.lod3Types != null)
			tag.put(LOD3_TYPES, serializeLODTypes(this.lod3Types));
		
		tag.putInt(LOD1_WEIGHT, lod1Weight);
		tag.putInt(LOD2_WEIGHT, lod2Weight);
		tag.putInt(LOD3_WEIGHT, lod3Weight);
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		this.starTexture = new ResourceLocation(tag.getString(STAR_TEXTURE));
		
		this.lod1Types = getLODTypes(tag, LOD1_TYPES);
		this.lod2Types = getLODTypes(tag, LOD2_TYPES);
		this.lod3Types = getLODTypes(tag, LOD3_TYPES);
		
		this.lod1Weight = tag.getInt(LOD1_WEIGHT);
		this.lod2Weight = tag.getInt(LOD2_WEIGHT);
		this.lod3Weight = tag.getInt(LOD3_WEIGHT);
	}
}
