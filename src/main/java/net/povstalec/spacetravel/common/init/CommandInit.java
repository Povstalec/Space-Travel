package net.povstalec.spacetravel.common.init;

import java.util.Map;
import java.util.Optional;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.povstalec.spacetravel.SpaceTravel;
import net.povstalec.spacetravel.common.capabilities.SpaceshipCapability;
import net.povstalec.spacetravel.common.capabilities.SpaceshipCapabilityProvider;
import net.povstalec.spacetravel.common.data.Multiverse;
import net.povstalec.spacetravel.common.packets.ClientBoundRenderCenterUpdatePacket;
import net.povstalec.spacetravel.common.packets.ClientBoundSpaceRegionClearPacket;
import net.povstalec.spacetravel.common.packets.ClientBoundSpaceRegionLoadPacket;
import net.povstalec.spacetravel.common.space.SpaceRegion;
import net.povstalec.spacetravel.common.space.Spaceship;
import net.povstalec.spacetravel.common.space.Universe;
import net.povstalec.spacetravel.common.util.DimensionUtil;

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
				.then(Commands.literal("spaceship")
					.then(Commands.literal("toggle").executes(CommandInit::toggleSpaceship))
					.requires(commandSourceStack -> commandSourceStack.hasPermission(2))));
		
		// Client commands
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
		ServerLevel level = context.getSource().getLevel();

		PacketHandlerInit.sendToPlayer(player, new ClientBoundRenderCenterUpdatePacket(new Spaceship())); //TODO Get coords from somewhere
		if(player != null && level != null)
		{
			LazyOptional<SpaceshipCapability> spaceshipCapability = level.getCapability(SpaceshipCapabilityProvider.SPACESHIP);
			
			spaceshipCapability.ifPresent(cap -> 
			{
				if(cap != null)
				{
					Optional<Universe> universe = Multiverse.get(level).getUniverse("main");
					
					if(universe.isPresent())
					{
						PacketHandlerInit.sendToPlayer(player, new ClientBoundSpaceRegionClearPacket());
						for(Map.Entry<SpaceRegion.Position, SpaceRegion> spaceRegionEntry : universe.get().getRegionsAt(new SpaceRegion.Position(cap.spaceship.getSpaceCoords()), 1).entrySet()) //TODO Get coords from somewhere
						{
							PacketHandlerInit.sendToPlayer(player, new ClientBoundSpaceRegionLoadPacket(spaceRegionEntry.getValue())); //TODO Get coords from somewhere
						}
					}
				}
			});
			
			context.getSource().sendSuccess(() -> Component.literal("Reloaded renderer"), false); //TODO Translation
		}
		
		return Command.SINGLE_SUCCESS;
	}
	
	private static int toggleSpaceship(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		ServerLevel level = context.getSource().getLevel();
		
		if(level != null)
		{
			LazyOptional<SpaceshipCapability> spaceshipCapability = level.getCapability(SpaceshipCapabilityProvider.SPACESHIP);
			
			spaceshipCapability.ifPresent(cap -> 
			{
				if(cap != null)
					cap.spaceship.toggleSpeed();
			});
		}
		
		return Command.SINGLE_SUCCESS;
	}
}
