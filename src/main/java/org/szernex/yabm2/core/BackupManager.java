package org.szernex.yabm2.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import org.szernex.yabm2.util.BackupCreationHelper;
import org.szernex.yabm2.util.LogHelper;
import org.szernex.yabm2.util.ScheduleHelper;
import org.szernex.yabm2.util.WorldHelper;

import java.util.Date;

public class BackupManager
{
	private long nextScheduleTimestamp;

	private boolean schedule()
	{
		Date nextSchedule = ScheduleHelper.getNextSchedule();
		nextSchedule = new Date(System.currentTimeMillis() + 5000); // --------------- DEBUGGING

		if (nextSchedule == null)
		{
			LogHelper.error("No valid backup schedule found, check configuration!");
			return false;
		}

		nextScheduleTimestamp = nextSchedule.toInstant().toEpochMilli();
		LogHelper.info("Next scheduled backup: %s %d", nextSchedule, nextScheduleTimestamp);

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
			schedule();
			return;
		}

		// start backup
		LogHelper.info("Backup starting, prepare for lag");

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
