package net.povstalec.spacetravel.common.packets;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.povstalec.spacetravel.client.ClientAccess;
import net.povstalec.stellarview.api.common.space_objects.ViewObject;

public class ClientBoundRenderCenterUpdatePacket
{
	public final CompoundTag tag;

    public ClientBoundRenderCenterUpdatePacket(ViewObject renderCenter)
    {
        this.tag = renderCenter.serializeNBT();
    }

    private ClientBoundRenderCenterUpdatePacket(CompoundTag tag)
    {
        this.tag = tag;
    }

    public ClientBoundRenderCenterUpdatePacket(FriendlyByteBuf buffer)
    {
        this(buffer.readNbt());
    }

    public void encode(FriendlyByteBuf buffer)
    {
    	buffer.writeNbt(tag);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
        	ClientAccess.updateRenderCenter(tag);
        });
        
        ctx.get().setPacketHandled(true);
        return true;
    }
}
