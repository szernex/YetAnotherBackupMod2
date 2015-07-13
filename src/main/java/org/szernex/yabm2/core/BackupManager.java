package org.szernex.yabm2.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.world.WorldServer;
import org.szernex.yabm2.util.LogHelper;
import org.szernex.yabm2.util.ScheduleHelper;
import org.szernex.yabm2.util.WorldHelper;

import java.util.Date;
import java.util.HashMap;

public class BackupManager
{
	private Date nextSchedule = null;
	private long nextScheduleTimestamp;

	private boolean schedule()
	{
		nextSchedule = ScheduleHelper.getNextSchedule();
		nextSchedule = new Date(System.currentTimeMillis() + 10000); // --------------- DEBUGGING

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
		LogHelper.info("Initializing BackupManager"); //DEBUG

		nextScheduleTimestamp = Long.MAX_VALUE;

		schedule();
	}

	public void stop()
	{
		LogHelper.info("Stopping BackupManager"); //DEBUG

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
		HashMap<WorldServer, Boolean> save_flags = WorldHelper.disableWorldSaving();

		// start new backup thread
		BackupThread backup_thread = new BackupThread();

		LogHelper.info("Starting backup thread");
		backup_thread.setWorldSaveFlags(save_flags);
		backup_thread.start();

		// re-schedule next backup
		schedule();
	}
}
