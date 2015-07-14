package org.szernex.yabm2.core;

import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.szernex.yabm2.handler.ConfigHandler;
import org.szernex.yabm2.util.BackupCreationHelper;
import org.szernex.yabm2.util.BackupManagingHelper;
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


		// prepare directory structures
		LogHelper.info("Preparing directories");

		Path backup_dir = Paths.get(ConfigHandler.backupPath).toAbsolutePath().normalize();
		Path persistent_dir = Paths.get(ConfigHandler.persistentPath).toAbsolutePath().normalize();

		try
		{
			LogHelper.debug("Preparing backup directory: " + backup_dir);
			Files.createDirectories(backup_dir);


			if (ConfigHandler.persistentBackups)
			{
				LogHelper.debug("Preparing persistent directory: " + persistent_dir);
				Files.createDirectories(persistent_dir);
			}
		}
		catch (IOException ex)
		{
			LogHelper.error("Could not create directory: %s. Aborting backup.", ex.getMessage());
			ex.printStackTrace();
			finish();
			return;
		}


		// determine if persistent backup
		Path target_dir = backup_dir;
		boolean persistent = false;

		if (ConfigHandler.persistentBackups && BackupManagingHelper.isPersistent())
		{
			target_dir = persistent_dir;
			persistent = true;
		}


		// create backup archive
		Path root = Paths.get("");
		Path target_file = target_dir.resolve(BackupCreationHelper.generateArchiveFileName(persistent));
		Path world_path = root.resolve(DimensionManager.getCurrentSaveRootDirectory().toString()).normalize();

		LogHelper.info("Backup info: Root: %s - World directory: %s - Target backup file: %s", root.toAbsolutePath(), world_path, target_file);

		try
		{
			BackupCreationHelper.createZipArchive(target_file, root, world_path);
		}
		catch (IOException ex)
		{
			LogHelper.error("Error creating backup archive: %s", ex.getMessage());
			ex.printStackTrace();
			finish();
			return;
		}


		// consolidate old backups
		BackupManagingHelper.consolidateBackups();


		// finish up
		finish();

		LogHelper.info("Task ran for %dms", (System.currentTimeMillis() - start_time));
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
