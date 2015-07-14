package org.szernex.yabm2.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import org.szernex.yabm2.YABM2;
import org.szernex.yabm2.util.ChatHelper;
import org.szernex.yabm2.util.LogHelper;

public class CommandStartBackup extends CommandBase
{
	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	@Override
	public String getCommandName()
	{
		return null;
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return null;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		LogHelper.info(sender.getCommandSenderName() + " manually started a backup");
		ChatHelper.sendLocalizedUserChatMsg(sender, "commands.yabm2.startbackup.started", sender.getCommandSenderName());
		YABM2.backupManager.startBackup();
	}
}
