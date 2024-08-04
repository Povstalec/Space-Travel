package net.povstalec.spacetravel.common.packets;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.povstalec.spacetravel.client.ClientAccess;
import net.povstalec.spacetravel.common.space.Spaceship;

public class ClientBoundSpaceshipUpdatePacket
{
	public final CompoundTag spaceshipTag;

    public ClientBoundSpaceshipUpdatePacket(Spaceship spaceship)
    {
        this.spaceshipTag = spaceship.serializeNBT();
    }

    private ClientBoundSpaceshipUpdatePacket(CompoundTag spaceshipTag)
    {
        this.spaceshipTag = spaceshipTag;
    }

    public ClientBoundSpaceshipUpdatePacket(FriendlyByteBuf buffer)
    {
        this(buffer.readNbt());
    }

    public void encode(FriendlyByteBuf buffer)
    {
    	buffer.writeNbt(spaceshipTag);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
        	ClientAccess.updateSpaceship(spaceshipTag);
        });
        
        ctx.get().setPacketHandled(true);
        return true;
    }
}
