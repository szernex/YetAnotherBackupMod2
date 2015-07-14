package org.szernex.yabm2.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import org.szernex.yabm2.util.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class BackupManager
{
	private long nextScheduleTimestamp;

	private boolean schedule()
	{
		nextScheduleTimestamp = Long.MAX_VALUE;

		ZonedDateTime nextSchedule = ScheduleHelper.getNextSchedule();

		if (nextSchedule == null)
		{
			LogHelper.error("No valid backup schedule found, check configuration!");
			ChatHelper.sendLocalizedServerChatMsg("yabm2.backup.error.no_schedule");
			return false;
		}

		nextScheduleTimestamp = nextSchedule.toInstant().toEpochMilli();

		String format = nextSchedule.format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss '['z']'"));

		LogHelper.info("Next scheduled backup: %s", format);
		ChatHelper.sendLocalizedServerChatMsg("yabm2.backup.general.next_backup", format);

		return true;
	}

	public void init()
	{
		LogHelper.info("Initializing BackupManager");

		BackupCreationHelper.init();

		nextScheduleTimestamp = Long.MAX_VALUE;

		if (!schedule())
			LogHelper.warn("BackupManager failed to initialize, backups will not be run!");
	}

	public void stop()
	{
		LogHelper.info("Stopping BackupManager");

	}

	public void startBackup()
	{
		nextScheduleTimestamp = System.currentTimeMillis() + 1;
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event)
	{
		// check to skip this tick
		if (event.phase == TickEvent.Phase.START)
			return;

		if (System.currentTimeMillis() < nextScheduleTimestamp)
			return;

		if (BackupThread.backupLock.isLocked())
		{
			LogHelper.warn("Skipping to next scheduled backup because previous thread hasn't finished yet");
			ChatHelper.sendLocalizedServerChatMsg("yabm2.backup.error.thread_still_running");
			schedule();
			return;
		}

		// start backup
		LogHelper.info("Backup starting, prepare for lag");
		ChatHelper.sendLocalizedServerChatMsg("yabm2.backup.general.backup_starting");

		// turn off auto-save
		WorldHelper.disableWorldSaving();

		// start new backup thread
		BackupThread backup_thread = new BackupThread();

		LogHelper.info("Starting backup thread");
		backup_thread.start();

		// re-schedule next backup
		schedule();
	}
}
