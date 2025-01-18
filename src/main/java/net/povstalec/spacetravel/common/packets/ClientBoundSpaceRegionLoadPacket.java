package net.povstalec.spacetravel.common.packets;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.povstalec.spacetravel.client.ClientAccess;
import net.povstalec.spacetravel.common.space.STSpaceRegion;

public class ClientBoundSpaceRegionLoadPacket
{
	public final long x;
	public final long y;
	public final long z;
	public final CompoundTag childrenTag;
	
	private ClientBoundSpaceRegionLoadPacket(long x, long y, long z, CompoundTag childrenTag)
    {
		this.x = x;
		this.y = y;
		this.z = z;
        this.childrenTag = childrenTag;
    }
	
	public ClientBoundSpaceRegionLoadPacket(STSpaceRegion spaceRegion)
    {
		this(spaceRegion.getRegionPos().x(), spaceRegion.getRegionPos().y(), spaceRegion.getRegionPos().z(), spaceRegion.getChildrenTag());
    }

    public ClientBoundSpaceRegionLoadPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readLong(), buffer.readLong(), buffer.readLong(), buffer.readNbt());
    }

    public void encode(FriendlyByteBuf buffer)
    {
    	buffer.writeLong(x);
    	buffer.writeLong(y);
    	buffer.writeLong(z);
    	buffer.writeNbt(childrenTag);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
        	ClientAccess.loadSpaceRegion(x, y, z, childrenTag);
        });
        
        ctx.get().setPacketHandled(true);
        return true;
    }
}
