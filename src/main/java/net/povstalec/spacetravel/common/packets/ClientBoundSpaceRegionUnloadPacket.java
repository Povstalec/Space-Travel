package net.povstalec.spacetravel.common.packets;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.povstalec.spacetravel.client.ClientAccess;
import net.povstalec.spacetravel.common.space.STSpaceRegion;
import net.povstalec.stellarview.api.common.SpaceRegion;

public class ClientBoundSpaceRegionUnloadPacket
{
	public final long x;
	public final long y;
	public final long z;
	
	private ClientBoundSpaceRegionUnloadPacket(long x, long y, long z)
    {
		this.x = x;
		this.y = y;
		this.z = z;
    }
	
	public ClientBoundSpaceRegionUnloadPacket(SpaceRegion.RegionPos spaceRegionPos)
    {
		this(spaceRegionPos.x(), spaceRegionPos.y(), spaceRegionPos.z());
    }

    public ClientBoundSpaceRegionUnloadPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readLong(), buffer.readLong(), buffer.readLong());
    }

    public void encode(FriendlyByteBuf buffer)
    {
    	buffer.writeLong(x);
    	buffer.writeLong(y);
    	buffer.writeLong(z);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
        	ClientAccess.unloadSpaceRegion(x, y, z);
        });
        
        ctx.get().setPacketHandled(true);
        return true;
    }
}
