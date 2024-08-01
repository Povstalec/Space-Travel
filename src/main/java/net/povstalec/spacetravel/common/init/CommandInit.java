package net.povstalec.spacetravel.common.init;

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
import net.minecraftforge.event.RegisterCommandsEvent;
import net.povstalec.spacetravel.SpaceTravel;
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
	}
	
	private static int createRandomDimension(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		MinecraftServer server = context.getSource().getServer();
		
		DimensionUtil.createSpaceship(server);
		context.getSource().sendSuccess(() -> Component.literal("Created a new Dimension"), false);
		
		return Command.SINGLE_SUCCESS;
	}
	
	private static int createNamedDimension(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		MinecraftServer server = context.getSource().getServer();
		String name = StringArgumentType.getString(context, "name");
		
		if(ResourceLocation.isValidPath(name)) // TODO Check for existing dimensions
		{
			DimensionUtil.createSpaceship(server, name);
			context.getSource().sendSuccess(() -> Component.literal("Created a new Dimension"), false);
		}
		
		return Command.SINGLE_SUCCESS;
	}
}
