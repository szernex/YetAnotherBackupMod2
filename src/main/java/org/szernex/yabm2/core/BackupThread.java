package org.szernex.yabm2.core;

import net.minecraftforge.common.DimensionManager;
import org.szernex.yabm2.handler.ConfigHandler;
import org.szernex.yabm2.util.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;

public class BackupThread extends Thread
{
	public static final ReentrantLock backupLock = new ReentrantLock();

	@Override
	public void run()
	{
		LogHelper.info("BACKUPTASK IS RUNNING");


		// acquire lock
		LogHelper.info("Trying to acquire backup lock");
		if (!backupLock.tryLock())
		{
			LogHelper.error("Unable to acquire backup lock, aborting");
			ChatHelper.sendLocalizedServerChatMsg("yabm2.backup.error.lock_failed");
			finish();
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
			ChatHelper.sendLocalizedServerChatMsg("yabm2.backup.error.dir_creation_failed");
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
			ChatHelper.sendLocalizedServerChatMsg("yabm2.backup.error.archive_creation_failed", ex.getMessage());
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
		WorldHelper.enableWorldSaving();


		// release lock
		LogHelper.info("Releasing backup lock");
		LogHelper.info("TASK FINISHED");
		ChatHelper.sendLocalizedServerChatMsg("yabm2.backup.general.backup_finished");
		backupLock.unlock();
	}
}
