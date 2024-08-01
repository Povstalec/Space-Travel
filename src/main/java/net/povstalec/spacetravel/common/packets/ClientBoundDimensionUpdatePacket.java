package net.povstalec.spacetravel.common.packets;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.povstalec.spacetravel.client.ClientAccess;

public class ClientBoundDimensionUpdatePacket
{
	public final Set<ResourceKey<Level>> keys;
    public final boolean add;

    public ClientBoundDimensionUpdatePacket(Set<ResourceKey<Level>> keys, boolean add)
    {
        this.keys = keys;
        this.add = add;
    }

    public ClientBoundDimensionUpdatePacket(FriendlyByteBuf buffer)
    {
        this(buffer.readCollection(i -> new HashSet<>(), buf -> ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation())), buffer.readBoolean());
    }

    public void encode(FriendlyByteBuf buffer)
    {
    	buffer.writeCollection(this.keys, (buf,key)->buf.writeResourceLocation(key.location()));
        buffer.writeBoolean(this.add);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
        	ClientAccess.updateDimensions(keys, add);
        });
        return true;
    }
}
