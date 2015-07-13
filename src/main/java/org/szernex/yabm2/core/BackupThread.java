package org.szernex.yabm2.core;

import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.szernex.yabm2.handler.ConfigHandler;
import org.szernex.yabm2.util.FileHelper;
import org.szernex.yabm2.util.LogHelper;
import org.szernex.yabm2.util.WorldHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

		// initialize folder structure if necessary
		Path root = Paths.get("");
		Path target_dir = root.toAbsolutePath().resolve(Paths.get(ConfigHandler.backupPath)).normalize();

		try
		{
			Files.createDirectories(target_dir);
		}
		catch (IOException ex)
		{
			LogHelper.error("Error while trying to create backup directory structure: %s. Aborting backup.", ex.getMessage());
			ex.printStackTrace();
			finish();
		}

		// create backup archive
		Path target_file = target_dir.resolve(FileHelper.generateArchiveFileName());
		Path world_path = root.resolve(DimensionManager.getCurrentSaveRootDirectory().toString()).normalize();

		try
		{
			FileHelper.createZipArchive(target_file, root, world_path);
		}
		catch (IOException ex)
		{
			LogHelper.error("Error creating backup archive: %s", ex.getMessage());
			ex.printStackTrace();
			finish();
		}

		// consolidate old backups

		// finish up
		finish();

		LogHelper.info("Task ran for %d ms", (System.currentTimeMillis() - start_time));
	}

	private void finish()
	{
		// turn auto-save on
		WorldHelper.enableWorldSaving(worldSaveFlags);

		// release lock
		LogHelper.info("Releasing backup lock");
		LogHelper.info("TASK FINISHED");
		backupLock.unlock();
	}
}
