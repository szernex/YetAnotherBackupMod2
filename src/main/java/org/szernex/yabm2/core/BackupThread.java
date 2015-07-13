package org.szernex.yabm2.core;

import net.minecraft.world.WorldServer;
import org.szernex.yabm2.util.FileHelper;
import org.szernex.yabm2.util.LogHelper;
import org.szernex.yabm2.util.WorldHelper;

import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class BackupThread extends Thread
{
	public static final ReentrantLock backupLock = new ReentrantLock();

	private HashMap<WorldServer, Boolean> worldSaveFlags = null;

	public void setWorldSaveFlags(HashMap<WorldServer, Boolean> worldSaveFlags)
	{
		this.worldSaveFlags = worldSaveFlags;
	}

	@Override
	public void run()
	{
		LogHelper.info("BACKUPTASK IS RUNNING");

		// acquire lock
		LogHelper.info("Trying to acquire backup lock");
		if (!backupLock.tryLock())
		{
			LogHelper.error("Unable to acquire backup lock, aborting");
			return;
		}

		if (worldSaveFlags == null)
		{
			LogHelper.error("World saving flags have not been set, aborting backup");
			return;
		}

		long start_time = System.currentTimeMillis();

		// determine if persistent backup

		// gather files
		FileHelper.gatherFiles(FileSystems.getDefault().getPath("."));

		// create archive

		// turn auto-save on
		WorldHelper.enableWorldSaving(worldSaveFlags);

		// consolidate old backups

		// release lock
		LogHelper.info("Releasing backup lock");
		LogHelper.info("TASK FINISHED");
		backupLock.unlock();

		LogHelper.info("Task ran for %d milliseconds", (System.currentTimeMillis() - start_time));
	}
}
