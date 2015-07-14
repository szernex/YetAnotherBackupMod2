package org.szernex.yabm2.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import org.szernex.yabm2.YABM2;
import org.szernex.yabm2.core.BackupThread;
import org.szernex.yabm2.handler.ConfigHandler;
import org.szernex.yabm2.util.ChatHelper;
import org.szernex.yabm2.util.LogHelper;

public class CommandReloadConfig extends CommandBase
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
		LogHelper.info("Attempting to reload configuration");

		if (BackupThread.backupLock.isLocked())
		{
			LogHelper.warn("A backup thread appears to be running, can't reload configuration now!");
			ChatHelper.sendLocalizedUserChatMsg(sender, "commands.yabm2.reloadconfig.error.thread_running");
			return;
		}

		ConfigHandler.init(YABM2.configFile);
		YABM2.backupManager.init();

		LogHelper.info("Configuration reloaded");
		ChatHelper.sendLocalizedUserChatMsg(sender, "commands.yabm2.reloadconfig.success");
	}
}
