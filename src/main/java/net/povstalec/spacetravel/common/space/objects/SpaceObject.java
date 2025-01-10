package net.povstalec.spacetravel.common.space.objects;

import java.util.ArrayList;
import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.povstalec.spacetravel.common.util.AxisRot;
import net.povstalec.spacetravel.common.util.SpacePos;
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

public class SpaceObject implements INBTSerializable<CompoundTag>
{
	public static final String OBJECT_TYPE = "object_type";
	
	public static final String PARENT_KEY = "parent_key";
	
	public static final String CHILDREN = "children";
	//TODO Other children types
	
	public static final String COORDS = "coords";
	public static final String AXIS_ROTATION = "axis_rotation";
	
	public static final String FADE_OUT_HANDLER = "fade_out_handler";

	public static final String ID = "id";
	
	public static final ResourceLocation SPACE_OBJECT_LOCATION = new ResourceLocation(SpaceTravel.MODID, "celestials/space_object");
	public static final ResourceKey<Registry<SpaceObject>> REGISTRY_KEY = ResourceKey.createRegistryKey(SPACE_OBJECT_LOCATION);
	public static final Codec<ResourceKey<SpaceObject>> RESOURCE_KEY_CODEC = ResourceKey.codec(REGISTRY_KEY);
	
	private ResourceLocation objectType;
	
	@Nullable
	protected ResourceLocation parentLocation;
	@Nullable
	protected SpaceObject parent;
	
	protected ArrayList<SpaceObject> children = new ArrayList<SpaceObject>();
	
	protected SpacePos coords; // Absolute coordinates of the center (not necessarily the object itself, since it can be orbiting some other object for example)
	protected AxisRot axisRot;
	
	protected FadeOutHandler fadeOutHandler;
	
	protected ResourceLocation location;
	
	public double lastDistance = 0; // Last known distance of this object from the View Center, used for sorting
	
	public SpaceObject(){}
	
	public SpaceObject(ResourceLocation objectType, Optional<ResourceLocation> parentLocation, Either<SpacePos, StellarCoordinates.Equatorial> coords,
					   AxisRot axisRot, FadeOutHandler fadeOutHandler)
	{
		this.objectType = objectType;
		
		if(parentLocation.isPresent())
			this.parentLocation = parentLocation.get();
		
		if(coords.left().isPresent())
			this.coords = coords.left().get();
		else
			this.coords = coords.right().get().toGalactic().toSpaceCoords();
		
		this.axisRot = axisRot;
		
		this.fadeOutHandler = fadeOutHandler;
	}
	
	public ResourceLocation getObjectType()
	{
		return objectType;
	}
	
	public boolean isInitialized()
	{
		return objectType != null && coords != null && axisRot != null;
	}
	
	public SpacePos getSpaceCoords()
	{
		return this.coords;
	}
	
	public SpacePos addSpaceCoords(SpacePos other)
	{
		return this.coords.add(other);
	}
	
	public Vector3f getPosition(boolean canClamp, AxisRot axisRot, long ticks, float partialTicks)
	{
		return new Vector3f();
	}
	
	public Vector3f getPosition(boolean canClamp, long ticks, float partialTicks)
	{
		return new Vector3f();
	}
	
	public AxisRot getAxisRotation()
	{
		return axisRot;
	}
	
	public Optional<ResourceLocation> getParentLocation()
	{
		return Optional.ofNullable(parentLocation);
	}
	
	public Optional<SpaceObject> getParent()
	{
		return Optional.ofNullable(parent);
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
	
	public void addExistingChild(SpaceObject child)
	{
		if(child.parent != null)
		{
			SpaceTravel.LOGGER.error(this.toString() + " already has a parent");
			return;
		}
		
		this.children.add(child);
		child.parent = this;
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
		
		child.axisRot = child.axisRot.add(this.axisRot);
		
		child.addCoordsAndRotationToChildren(this.coords, this.axisRot);
	}
	
	public ArrayList<SpaceObject> getChildren()
	{
		return children;
	}
	
	protected void addCoordsAndRotationToChildren(SpacePos coords, AxisRot axisRot)
	{
		for(SpaceObject childOfChild : this.children)
		{
			childOfChild.coords = childOfChild.coords.add(coords);
			childOfChild.axisRot = childOfChild.axisRot.add(axisRot);
			
			childOfChild.addCoordsAndRotationToChildren(coords, axisRot);
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
	
	protected void toBufferAdditional(FriendlyByteBuf buffer)
	{
	
	}
	
	public final void toBuffer(FriendlyByteBuf buffer)
	{
		buffer.writeResourceLocation(objectType);
		
		toBufferAdditional(buffer);
	}
	
	public void writeToBuffer(FriendlyByteBuf buffer)
	{
		buffer.writeResourceLocation(objectType);
		buffer.writeOptional(Optional.ofNullable(parentLocation), (buf, location) -> buf.writeResourceLocation(location));
		coords.writeToBuffer(buffer);
		axisRot.writeToBuffer(buffer);
		fadeOutHandler.writeToBuffer(buffer);
	}
	
	public static SpaceObject readFromBuffer(FriendlyByteBuf buffer)
	{
		return new SpaceObject(buffer.readResourceLocation(), buffer.readOptional((buf) -> buf.readResourceLocation()), Either.left(SpacePos.readFromBuffer(buffer)), AxisRot.readFromBuffer(buffer), FadeOutHandler.readFromBuffer(buffer));
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
		
		if(parentLocation != null)
			tag.putString(PARENT_KEY, parentLocation.toString());
		
		tag.put(COORDS, coords.serializeNBT());
		
		tag.put(AXIS_ROTATION, axisRot.serializeNBT());

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
		
		if(tag.contains(PARENT_KEY))
			this.parentLocation = new ResourceLocation(tag.getString(PARENT_KEY));
		
		this.coords = new SpacePos();
		coords.deserializeNBT(tag.getCompound(COORDS));
		
		this.axisRot = new AxisRot();
		axisRot.deserializeNBT(tag.getCompound(AXIS_ROTATION));
		
		// Deserialize Children
		CompoundTag childrenTag = tag.getCompound(CHILDREN);
		for(int j = 0; j < childrenTag.size(); j++)
		{
			CompoundTag childTag = childrenTag.getCompound(String.valueOf(j));
			
			SpaceObject spaceObject = SpaceObjectDeserializer.deserialize(childTag.getString(SpaceObject.OBJECT_TYPE), childTag);
			
			if(spaceObject != null && spaceObject.isInitialized())
				addExistingChild(spaceObject);
		}
		
		this.fadeOutHandler = new FadeOutHandler();
		this.fadeOutHandler.deserializeNBT(tag.getCompound(FADE_OUT_HANDLER));
	}
	
	public static class FadeOutHandler implements INBTSerializable<CompoundTag>
	{
		public static final String FADE_OUT_START_DISTANCE = "fade_out_start_distance";
		public static final String FADE_OUT_END_DISTANCE = "fade_out_end_distance";
		public static final String MAX_CHILD_RENDER_DISTANCE = "max_child_render_distance";
		
		public static final FadeOutHandler DEFAULT_PLANET_HANDLER = new FadeOutHandler(new SpacePos.SpaceDist(70000000000D), new SpacePos.SpaceDist(100000000000D), new SpacePos.SpaceDist(100000000000D));
		public static final FadeOutHandler DEFAULT_STAR_HANDLER = new FadeOutHandler(new SpacePos.SpaceDist(3000000L), new SpacePos.SpaceDist(5000000L), new SpacePos.SpaceDist(100000000000D));
		public static final FadeOutHandler DEFAULT_STAR_FIELD_HANDLER = new FadeOutHandler(new SpacePos.SpaceDist(3000000L), new SpacePos.SpaceDist(5000000L), new SpacePos.SpaceDist(5000000L));
		
		private SpacePos.SpaceDist fadeOutStartDistance;
		private SpacePos.SpaceDist fadeOutEndDistance;
		private SpacePos.SpaceDist maxChildRenderDistance;
		
		public static final Codec<FadeOutHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				SpacePos.SpaceDist.CODEC.fieldOf(FADE_OUT_START_DISTANCE).forGetter(FadeOutHandler::getFadeOutStartDistance),
				SpacePos.SpaceDist.CODEC.fieldOf(FADE_OUT_END_DISTANCE).forGetter(FadeOutHandler::getFadeOutEndDistance),
				SpacePos.SpaceDist.CODEC.fieldOf(MAX_CHILD_RENDER_DISTANCE).forGetter(FadeOutHandler::getMaxChildRenderDistance)
		).apply(instance, FadeOutHandler::new));
		
		public FadeOutHandler() {};
		
		public FadeOutHandler(SpacePos.SpaceDist fadeOutStartDistance, SpacePos.SpaceDist fadeOutEndDistance, SpacePos.SpaceDist maxChildRenderDistance)
		{
			this.fadeOutStartDistance = fadeOutStartDistance;
			this.fadeOutEndDistance = fadeOutEndDistance;
			this.maxChildRenderDistance = maxChildRenderDistance;
		}
		
		public SpacePos.SpaceDist getFadeOutStartDistance()
		{
			return fadeOutStartDistance;
		}
		
		public SpacePos.SpaceDist getFadeOutEndDistance()
		{
			return fadeOutEndDistance;
		}
		
		public SpacePos.SpaceDist getMaxChildRenderDistance()
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
			return new FadeOutHandler(SpacePos.SpaceDist.readFromBuffer(buffer), SpacePos.SpaceDist.readFromBuffer(buffer), SpacePos.SpaceDist.readFromBuffer(buffer));
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
			fadeOutStartDistance = new SpacePos.SpaceDist(0);
			fadeOutStartDistance.deserializeNBT(tag.getCompound(FADE_OUT_START_DISTANCE));
			
			fadeOutEndDistance = new SpacePos.SpaceDist(0);
			fadeOutEndDistance.deserializeNBT(tag.getCompound(FADE_OUT_END_DISTANCE));
			
			maxChildRenderDistance = new SpacePos.SpaceDist(0);
			maxChildRenderDistance.deserializeNBT(tag.getCompound(MAX_CHILD_RENDER_DISTANCE));
		}
	}
}
