package net.povstalec.spacetravel.common.init;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.packets.ClientBoundDimensionUpdatePacket;
import net.povstalec.spacetravel.common.packets.ClientBoundRenderCenterUpdatePacket;
import net.povstalec.spacetravel.common.packets.ClientBoundSpaceRegionUpdatePacket;

public final class PacketHandlerInit
{
	private static final String PROTOCOL_VERSION = "1.0";
	
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(SpaceTravel.MODID, "main_network"), 
			() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static int packetId = 0;
    
	private PacketHandlerInit(){}

    public static void sendPacketToAll(Object message)
    {
    	PacketHandlerInit.INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

    public static void sendPacketToDimension(ResourceKey<Level> level, Object message)
    {
        INSTANCE.send(PacketDistributor.DIMENSION.with(() -> level), message);
    }

    public static void sendToTracking(Entity e, Object message)
    {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> e), message);
    }

    public static void sendToTracking(BlockEntity tile, Object message)
    {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> tile.getLevel().getChunkAt(tile.getBlockPos())), message);
    }

    public static void sendTo(ServerPlayer player, Object message)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToServer(Object mes)
    {
        INSTANCE.sendToServer(mes);
    }

    public static int id()
    {
        return ++packetId;
    }
	
	public static void register()
	{
		int index = 0;
		
		//============================================================================================
		//****************************************Client-bound****************************************
		//============================================================================================
		
		INSTANCE.messageBuilder(ClientBoundDimensionUpdatePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(ClientBoundDimensionUpdatePacket::encode)
		.decoder(ClientBoundDimensionUpdatePacket::new)
		.consumerMainThread(ClientBoundDimensionUpdatePacket::handle)
		.add();
		
		INSTANCE.messageBuilder(ClientBoundSpaceRegionUpdatePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(ClientBoundSpaceRegionUpdatePacket::encode)
		.decoder(ClientBoundSpaceRegionUpdatePacket::new)
		.consumerMainThread(ClientBoundSpaceRegionUpdatePacket::handle)
		.add();
		
		INSTANCE.messageBuilder(ClientBoundRenderCenterUpdatePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(ClientBoundRenderCenterUpdatePacket::encode)
		.decoder(ClientBoundRenderCenterUpdatePacket::new)
		.consumerMainThread(ClientBoundRenderCenterUpdatePacket::handle)
		.add();
	}
}
