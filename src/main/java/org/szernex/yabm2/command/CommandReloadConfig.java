package org.szernex.yabm2.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import org.szernex.yabm2.YABM;
import org.szernex.yabm2.core.BackupThread;
import org.szernex.yabm2.handler.ConfigHandler;
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
			return;
		}

		ConfigHandler.init(YABM.configFile);
		YABM.backupManager.init();

		LogHelper.info("Configuration reloaded");
	}
}
