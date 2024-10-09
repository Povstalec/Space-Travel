package net.povstalec.spacetravel.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.spacetravel.common.space.objects.StarLike;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class StarInfo implements INBTSerializable<CompoundTag>
{
	public static final String STAR_TYPES = "star_types";
	public static final String TOTAL_WEIGHT = "total_weight";
	private ArrayList<StarLike.StarType> starTypes;
	private int totalWeight = 0;
	
	public static final StarLike.StarType O_CLASS = new StarLike.StarType(new Color.IntRGB(207, 207, 255), 0.25F, 0.35F, (short) 255, (short) 255, 1);
	public static final StarLike.StarType B_CLASS = new StarLike.StarType(new Color.IntRGB(223, 223, 255), 0.20F, 0.30F, (short) 225, (short) 255, 1);
	public static final StarLike.StarType A_CLASS = new StarLike.StarType(new Color.IntRGB(239, 239, 255), 0.15F, 0.25F, (short) 210, (short) 240, 1);
	public static final StarLike.StarType F_CLASS = new StarLike.StarType(new Color.IntRGB(255, 255, 255), 0.15F, 0.25F, (short) 190, (short) 230, 3);
	public static final StarLike.StarType G_CLASS = new StarLike.StarType(new Color.IntRGB(255, 255, 223), 0.10F, 0.25F, (short) 180, (short) 210, 7);
	public static final StarLike.StarType K_CLASS = new StarLike.StarType(new Color.IntRGB(255, 239, 223), 0.10F, 0.25F, (short) 120, (short) 200, 12);
	public static final StarLike.StarType M_CLASS = new StarLike.StarType(new Color.IntRGB(255, 223, 223), 0.10F, 0.25F, (short) 100, (short) 150, 74);
	public static final List<StarLike.StarType> DEFAULT_STARS = Arrays.asList(O_CLASS, B_CLASS, A_CLASS, F_CLASS, G_CLASS, K_CLASS, M_CLASS);
	public static final StarInfo DEFAULT_STAR_INFO = new StarInfo(DEFAULT_STARS);
	
	public static final Codec<StarInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			StarLike.StarType.CODEC.listOf().fieldOf("star_types").forGetter(starInfo -> starInfo.starTypes)
			).apply(instance, StarInfo::new));
	
	public StarInfo() {}
	
	public StarInfo(List<StarLike.StarType> starTypes)
	{
		this.starTypes = new ArrayList<StarLike.StarType>(starTypes);
		
		for(StarLike.StarType starType : starTypes)
		{
			this.totalWeight += starType.getWeight();
		}
	}
	
	public StarLike.StarType getRandomStarType(long seed)
	{
		Random random = new Random(seed);
		
		int i = 0;
		
		for(int weight = random.nextInt(0, totalWeight); i < starTypes.size() - 1; i++)
		{
			weight -= starTypes.get(i).getWeight();
			
			if(weight <= 0)
				break;
		}
		
		return starTypes.get(i);
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();
		
		CompoundTag starTypesTag = new CompoundTag();
		for(int i = 0; i < starTypes.size(); i++)
		{
			starTypesTag.put("star_type_" + i, starTypes.get(i).serializeNBT());
		}
		tag.put(STAR_TYPES, starTypesTag);
		
		tag.putInt(TOTAL_WEIGHT, totalWeight);
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		this.starTypes = new ArrayList<StarLike.StarType>();
		CompoundTag starTypesTag = tag.getCompound(STAR_TYPES);
		for(String key : starTypesTag.getAllKeys())
		{
			StarLike.StarType starType = new StarLike.StarType();
			starType.deserializeNBT(starTypesTag.getCompound(key));
			this.starTypes.add(starType);
			
		}
		
		totalWeight = tag.getInt(TOTAL_WEIGHT);
	}
}
