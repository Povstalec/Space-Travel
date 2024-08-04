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
import net.povstalec.spacetravel.common.packets.ClientBoundSpaceRegionClearPacket;
import net.povstalec.spacetravel.common.packets.ClientBoundSpaceRegionLoadPacket;
import net.povstalec.spacetravel.common.packets.ClientBoundSpaceRegionUnloadPacket;
import net.povstalec.spacetravel.common.packets.ClientBoundSpaceshipUpdatePacket;

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

    public static void sendPacketToDimension(ResourceKey<Level> levelKey, Object message)
    {
        INSTANCE.send(PacketDistributor.DIMENSION.with(() -> levelKey), message);
    }

    public static void sendToTracking(Entity entity, Object message)
    {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }

    public static void sendToTracking(BlockEntity blockEntity, Object message)
    {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> blockEntity.getLevel().getChunkAt(blockEntity.getBlockPos())), message);
    }

    public static void sendToPlayer(ServerPlayer player, Object message)
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
		
		INSTANCE.messageBuilder(ClientBoundSpaceRegionClearPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(ClientBoundSpaceRegionClearPacket::encode)
		.decoder(ClientBoundSpaceRegionClearPacket::new)
		.consumerMainThread(ClientBoundSpaceRegionClearPacket::handle)
		.add();
		
		INSTANCE.messageBuilder(ClientBoundSpaceRegionLoadPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(ClientBoundSpaceRegionLoadPacket::encode)
		.decoder(ClientBoundSpaceRegionLoadPacket::new)
		.consumerMainThread(ClientBoundSpaceRegionLoadPacket::handle)
		.add();
		
		INSTANCE.messageBuilder(ClientBoundSpaceRegionUnloadPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(ClientBoundSpaceRegionUnloadPacket::encode)
		.decoder(ClientBoundSpaceRegionUnloadPacket::new)
		.consumerMainThread(ClientBoundSpaceRegionUnloadPacket::handle)
		.add();
		
		INSTANCE.messageBuilder(ClientBoundRenderCenterUpdatePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(ClientBoundRenderCenterUpdatePacket::encode)
		.decoder(ClientBoundRenderCenterUpdatePacket::new)
		.consumerMainThread(ClientBoundRenderCenterUpdatePacket::handle)
		.add();
		
		INSTANCE.messageBuilder(ClientBoundSpaceshipUpdatePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(ClientBoundSpaceshipUpdatePacket::encode)
		.decoder(ClientBoundSpaceshipUpdatePacket::new)
		.consumerMainThread(ClientBoundSpaceshipUpdatePacket::handle)
		.add();
	}
}
