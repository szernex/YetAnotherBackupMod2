package org.szernex.yabm2.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import org.szernex.yabm2.util.ChatHelper;
import org.szernex.yabm2.util.StringHelper;

import java.util.*;

public class CommandYABM extends CommandBase
{
	private Map<String, CommandBase> availableCommands = new HashMap<>();

	public CommandYABM()
	{
		super();

		availableCommands.put("startbackup", new CommandStartBackup());
		availableCommands.put("reloadconfig", new CommandReloadConfig());
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	@Override
	public String getCommandName()
	{
		return "yabm";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return null;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		if (args.length == 0)
		{
			ChatHelper.sendLocalizedUserChatMsg(sender, "commands.yabm2.general.usage");
			return;
		}

		String sub_command = args[0].toLowerCase();

		if (availableCommands.containsKey(sub_command))
		{
			availableCommands.get(sub_command).processCommand(sender, Arrays.copyOfRange(args, 1, args.length));
		}
		else
		{
			ChatHelper.sendLocalizedUserChatMsg(sender, "commands.yabm2.error.invalid_key");
		}
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args)
	{
		if (args.length == 1)
		{
			return new ArrayList<>(StringHelper.getWordsStartingWith(args[0], availableCommands.keySet()));
		}

		String sub_command = args[0].toLowerCase();

		if (availableCommands.containsKey(sub_command))
		{
			return availableCommands.get(sub_command).addTabCompletionOptions(sender, Arrays.copyOfRange(args, 1, args.length));
		}

		return null;
	}
}
