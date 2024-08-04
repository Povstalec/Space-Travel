package net.povstalec.spacetravel.common.packets;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.povstalec.spacetravel.client.ClientAccess;

public class ClientBoundSpaceRegionClearPacket
{
	public ClientBoundSpaceRegionClearPacket() {}

    public ClientBoundSpaceRegionClearPacket(FriendlyByteBuf buffer) {}

    public void encode(FriendlyByteBuf buffer) {}

    public boolean handle(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
        	ClientAccess.clearSpaceRegion();
        });
        
        ctx.get().setPacketHandled(true);
        return true;
    }
}
