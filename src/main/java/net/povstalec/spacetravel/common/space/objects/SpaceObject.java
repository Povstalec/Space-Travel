package net.povstalec.spacetravel.common.space.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.povstalec.spacetravel.common.util.StellarCoordinates;
import org.joml.Vector3f;

import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.space.SpaceObjectDeserializer;
import net.povstalec.spacetravel.common.util.AxisRotation;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import net.povstalec.spacetravel.common.util.TextureLayer;

public class SpaceObject implements INBTSerializable<CompoundTag>
{
	public static final float DEFAULT_DISTANCE = 100.0F;
	
	public static final String OBJECT_TYPE = "object_type";
	
	public static final String PARENT_NAME = "parent_name";
	
	public static final String CHILDREN = "children";
	//TODO Other children types
	
	public static final String COORDS = "coords";
	public static final String AXIS_ROTATION = "axis_rotation";
	
	public static final String FADE_OUT_HANDLER = "fade_out_handler";

	public static final String ID = "id";
	
	private ResourceLocation objectType;
	
	@Nullable
	protected String parentName;
	@Nullable
	protected SpaceObject parent;
	
	protected ArrayList<SpaceObject> children = new ArrayList<SpaceObject>();
	
	protected SpaceCoords coords; // Absolute coordinates of the center (not necessarily the object itself, since it can be orbiting some other object for example)
	protected AxisRotation axisRotation;
	
	protected FadeOutHandler fadeOutHandler;
	
	protected ResourceLocation location;
	
	public double lastDistance = 0; // Last known distance of this object from the View Center, used for sorting
	
	public SpaceObject(){}
	
	public SpaceObject(ResourceLocation objectType, Optional<String> parentName, Either<SpaceCoords, StellarCoordinates.Equatorial> coords,
					   AxisRotation axisRotation, FadeOutHandler fadeOutHandler)
	{
		this.objectType = objectType;
		
		if(parentName.isPresent())
				this.parentName = parentName.get();
		
		if(coords.left().isPresent())
			this.coords = coords.left().get();
		else
			this.coords = coords.right().get().toGalactic().toSpaceCoords();
		
		this.axisRotation = axisRotation;
		
		this.fadeOutHandler = fadeOutHandler;
	}
	
	public ResourceLocation getObjectType()
	{
		return objectType;
	}
	
	public boolean isInitialized()
	{
		return objectType != null && coords != null && axisRotation != null;
	}
	
	public SpaceCoords getSpaceCoords()
	{
		return this.coords;
	}
	
	public SpaceCoords addSpaceCoords(SpaceCoords other)
	{
		return this.coords.add(other);
	}
	
	public Vector3f getPosition(boolean canClamp, AxisRotation axisRotation, long ticks, float partialTicks)
	{
		return new Vector3f();
	}
	
	public Vector3f getPosition(boolean canClamp, long ticks, float partialTicks)
	{
		return new Vector3f();
	}
	
	public AxisRotation getAxisRotation()
	{
		return axisRotation;
	}
	
	public Optional<String> getParentName()
	{
		if(parentName != null)
			return Optional.of(parentName);
		
		return Optional.empty();
	}
	
	public FadeOutHandler getFadeOutHandler()
	{
		return fadeOutHandler;
	}
	
	public void setResourceLocation(ResourceLocation resourceLocation)
	{
		this.location = resourceLocation;
	}
	
	public ResourceLocation getResourceLocation()
	{
		return this.location;
	}
	
	public static double distanceSize(double distance)
	{
		return 1 / distance;
	}
	
	public void addChild(SpaceObject child)
	{
		if(child.parent != null)
		{
			SpaceTravel.LOGGER.error(this.toString() + " already has a parent");
			return;
		}
		
		this.children.add(child);
		child.parent = this;
		child.coords = child.coords.add(this.coords);
		
		child.addCoordsToChildren(this.coords);
	}
	
	private void addCoordsToChildren(SpaceCoords coords)
	{
		for(SpaceObject childOfChild : this.children)
		{
			childOfChild.coords = childOfChild.coords.add(coords);
			childOfChild.addCoordsToChildren(coords);
		}
	}
	
	@Override
	public String toString()
	{
		if(location != null)
			return location.toString();
		
		return this.getClass().toString();
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	public void writeToBuffer(FriendlyByteBuf buffer)
	{
		buffer.writeResourceLocation(objectType);
		buffer.writeOptional(Optional.ofNullable(parentName), (buf, key) -> buf.writeUtf(key));
		coords.writeToBuffer(buffer);
		axisRotation.writeToBuffer(buffer);
		fadeOutHandler.writeToBuffer(buffer);
	}
	
	public static SpaceObject readFromBuffer(FriendlyByteBuf buffer)
	{
		return new SpaceObject(buffer.readResourceLocation(), buffer.readOptional((buf) -> buf.readUtf()), Either.left(SpaceCoords.readFromBuffer(buffer)), AxisRotation.readFromBuffer(buffer), FadeOutHandler.readFromBuffer(buffer));
	}
	
	@Override
	public boolean equals(Object other)
	{
		if(other == this)
			return true;
		else if(other instanceof SpaceObject spaceObject && spaceObject.location != null && spaceObject.location.equals(this.location))
			return true;
		
		return false;
	}

	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();
		
		if(location != null)
			tag.putString(ID, location.toString());
		
		tag.putString(OBJECT_TYPE, objectType.toString());
		
		if(parentName != null)
			tag.putString(PARENT_NAME, parentName);
		
		tag.put(COORDS, coords.serializeNBT());
		
		tag.put(AXIS_ROTATION, axisRotation.serializeNBT());

		// Serialize Children
		CompoundTag childrenTag = new CompoundTag();
		int j = 0;
		for(SpaceObject spaceObject : children)
		{
			childrenTag.put(String.valueOf(j), spaceObject.serializeNBT());
			j++;
		}
		tag.put(CHILDREN, childrenTag);
		
		tag.put(FADE_OUT_HANDLER, fadeOutHandler.serializeNBT());
		
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		objectType = new ResourceLocation(tag.getString(OBJECT_TYPE));
		
		if(tag.contains(ID))
			this.location = new ResourceLocation(tag.getString(ID));
		
		if(tag.contains(PARENT_NAME))
			this.parentName = tag.getString(PARENT_NAME);
		
		this.coords = new SpaceCoords();
		coords.deserializeNBT(tag.getCompound(COORDS));
		
		this.axisRotation = new AxisRotation();
		axisRotation.deserializeNBT(tag.getCompound(AXIS_ROTATION));
		
		// Deserialize Children
		CompoundTag childrenTag = tag.getCompound(CHILDREN);
		for(int j = 0; j < childrenTag.size(); j++)
		{
			CompoundTag childTag = childrenTag.getCompound(String.valueOf(j));
			
			SpaceObject spaceObject = SpaceObjectDeserializer.deserialize(childTag.getString(SpaceObject.OBJECT_TYPE), childTag);
			
			if(spaceObject != null && spaceObject.isInitialized())
				addChild(spaceObject);
		}
		
		this.fadeOutHandler = new FadeOutHandler();
		this.fadeOutHandler.deserializeNBT(tag.getCompound(FADE_OUT_HANDLER));
	}
	
	public static class FadeOutHandler implements INBTSerializable<CompoundTag>
	{
		public static final String FADE_OUT_START_DISTANCE = "fade_out_start_distance";
		public static final String FADE_OUT_END_DISTANCE = "fade_out_end_distance";
		public static final String MAX_CHILD_RENDER_DISTANCE = "max_child_render_distance";
		
		public static final FadeOutHandler DEFAULT_PLANET_HANDLER = new FadeOutHandler(new SpaceCoords.SpaceDistance(70000000000D), new SpaceCoords.SpaceDistance(100000000000D), new SpaceCoords.SpaceDistance(100000000000D));
		public static final FadeOutHandler DEFAULT_STAR_HANDLER = new FadeOutHandler(new SpaceCoords.SpaceDistance(3000000L), new SpaceCoords.SpaceDistance(5000000L), new SpaceCoords.SpaceDistance(100000000000D));
		public static final FadeOutHandler DEFAULT_STAR_FIELD_HANDLER = new FadeOutHandler(new SpaceCoords.SpaceDistance(3000000L), new SpaceCoords.SpaceDistance(5000000L), new SpaceCoords.SpaceDistance(5000000L));
		
		private SpaceCoords.SpaceDistance fadeOutStartDistance;
		private SpaceCoords.SpaceDistance fadeOutEndDistance;
		private SpaceCoords.SpaceDistance maxChildRenderDistance;
		
		public static final Codec<FadeOutHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				SpaceCoords.SpaceDistance.CODEC.fieldOf(FADE_OUT_START_DISTANCE).forGetter(FadeOutHandler::getFadeOutStartDistance),
				SpaceCoords.SpaceDistance.CODEC.fieldOf(FADE_OUT_END_DISTANCE).forGetter(FadeOutHandler::getFadeOutEndDistance),
				SpaceCoords.SpaceDistance.CODEC.fieldOf(MAX_CHILD_RENDER_DISTANCE).forGetter(FadeOutHandler::getMaxChildRenderDistance)
		).apply(instance, FadeOutHandler::new));
		
		public FadeOutHandler() {};
		
		public FadeOutHandler(SpaceCoords.SpaceDistance fadeOutStartDistance, SpaceCoords.SpaceDistance fadeOutEndDistance, SpaceCoords.SpaceDistance maxChildRenderDistance)
		{
			this.fadeOutStartDistance = fadeOutStartDistance;
			this.fadeOutEndDistance = fadeOutEndDistance;
			this.maxChildRenderDistance = maxChildRenderDistance;
		}
		
		public SpaceCoords.SpaceDistance getFadeOutStartDistance()
		{
			return fadeOutStartDistance;
		}
		
		public SpaceCoords.SpaceDistance getFadeOutEndDistance()
		{
			return fadeOutEndDistance;
		}
		
		public SpaceCoords.SpaceDistance getMaxChildRenderDistance()
		{
			return maxChildRenderDistance;
		}
		
		public void writeToBuffer(FriendlyByteBuf buffer)
		{
			fadeOutStartDistance.writeToBuffer(buffer);
			fadeOutEndDistance.writeToBuffer(buffer);
			maxChildRenderDistance.writeToBuffer(buffer);
		}
		
		public static FadeOutHandler readFromBuffer(FriendlyByteBuf buffer)
		{
			return new FadeOutHandler(SpaceCoords.SpaceDistance.readFromBuffer(buffer), SpaceCoords.SpaceDistance.readFromBuffer(buffer), SpaceCoords.SpaceDistance.readFromBuffer(buffer));
		}
		
		@Override
		public CompoundTag serializeNBT()
		{
			CompoundTag tag = new CompoundTag();
			
			tag.put(FADE_OUT_START_DISTANCE, fadeOutStartDistance.serializeNBT());
			tag.put(FADE_OUT_END_DISTANCE, fadeOutEndDistance.serializeNBT());
			tag.put(MAX_CHILD_RENDER_DISTANCE, maxChildRenderDistance.serializeNBT());
			
			return tag;
		}
		
		@Override
		public void deserializeNBT(CompoundTag tag)
		{
			fadeOutStartDistance = new SpaceCoords.SpaceDistance(0);
			fadeOutStartDistance.deserializeNBT(tag.getCompound(FADE_OUT_START_DISTANCE));
			
			fadeOutEndDistance = new SpaceCoords.SpaceDistance(0);
			fadeOutEndDistance.deserializeNBT(tag.getCompound(FADE_OUT_END_DISTANCE));
			
			maxChildRenderDistance = new SpaceCoords.SpaceDistance(0);
			maxChildRenderDistance.deserializeNBT(tag.getCompound(MAX_CHILD_RENDER_DISTANCE));
		}
	}
}
