package net.povstalec.spacetravel.common.init;

import java.util.ArrayList;
import java.util.Optional;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.packets.ClientBoundSpaceRegionUpdatePacket;
import net.povstalec.spacetravel.common.space.objects.Galaxy;
import net.povstalec.spacetravel.common.space.objects.Galaxy.SpiralGalaxy;
import net.povstalec.spacetravel.common.util.AxisRotation;
import net.povstalec.spacetravel.common.util.DimensionUtil;
import net.povstalec.spacetravel.common.util.SpaceCoords;
import net.povstalec.spacetravel.common.util.TextureLayer;

public class CommandInit
{
	public static void registerCommands(RegisterCommandsEvent event)
	{
		register(event.getDispatcher());
	}
	
	private static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		//Dev commands
		dispatcher.register(Commands.literal(SpaceTravel.MODID)
				.then(Commands.literal("dimension")
					.then(Commands.literal("create").executes(CommandInit::createRandomDimension)
							.then(Commands.argument("name", StringArgumentType.word()).executes(CommandInit::createNamedDimension))))
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2)));
		
		dispatcher.register(Commands.literal(SpaceTravel.MODID)
				.then(Commands.literal("render")
					.then(Commands.literal("reload").executes(CommandInit::reloadRenderer))));
	}
	
	private static int createRandomDimension(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		MinecraftServer server = context.getSource().getServer();
		
		DimensionUtil.createSpaceship(server);
		context.getSource().sendSuccess(() -> Component.literal("Created a new Dimension"), false); //TODO Translation
		
		return Command.SINGLE_SUCCESS;
	}
	
	private static int createNamedDimension(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		MinecraftServer server = context.getSource().getServer();
		String name = StringArgumentType.getString(context, "name");
		
		if(ResourceLocation.isValidPath(name)) // TODO Check for existing dimensions
		{
			DimensionUtil.createSpaceship(server, name);
			context.getSource().sendSuccess(() -> Component.literal("Created a new Dimension"), false); //TODO Translation
		}
		
		return Command.SINGLE_SUCCESS;
	}
	
	private static int reloadRenderer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		ServerPlayer player = context.getSource().getPlayer();
		
		if(player != null)
		{
			CompoundTag childrenTag = new CompoundTag();
			
	    	Galaxy.SpiralGalaxy milkyWay = new Galaxy.SpiralGalaxy(SpiralGalaxy.SPIRAL_GALAXY_LOCATION, Optional.empty(), new SpaceCoords(), new AxisRotation(), new ArrayList<TextureLayer>(), 10842, 90000, 4, 2.5, 1500);
	    	childrenTag.put("milky_way", milkyWay.serializeNBT());
	    	
			PacketHandlerInit.sendTo(player, new ClientBoundSpaceRegionUpdatePacket(0, 0, 0, childrenTag)); //TODO Get coords from somewhere
			context.getSource().sendSuccess(() -> Component.literal("Reloaded renderer"), false); //TODO Translation
		}
		
		return Command.SINGLE_SUCCESS;
	}
}
