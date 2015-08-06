package org.szernex.yabm2.core;

import net.minecraftforge.common.DimensionManager;
import org.apache.commons.net.ftp.FTPClient;
import org.szernex.yabm2.handler.ConfigHandler;
import org.szernex.yabm2.util.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

		LogHelper.info("Backup info:");
		LogHelper.info("  Root: " + root.toAbsolutePath());
		LogHelper.info("  World directory: " + world_path);
		LogHelper.info("  Target backup file: " + target_file);

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
		BackupManagingHelper.consolidateBackups(ConfigHandler.maxBackupCount, Paths.get(ConfigHandler.backupPath));
		BackupManagingHelper.consolidateBackups(ConfigHandler.maxPersistentCount, Paths.get(ConfigHandler.persistentPath));

		// handle FTP upload
		if (ConfigHandler.ftpEnabled)
		{
			if (ConfigHandler.ftpPersistentOnly)
			{
				if (!ConfigHandler.persistentBackups || persistent)
					uploadFTP(target_file);
			}
			else
			{
				uploadFTP(target_file);
			}
		}

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

	private void uploadFTP(Path target_file)
	{
		LogHelper.info("Starting FTP upload");
		ChatHelper.sendLocalizedServerChatMsg("yabm2.backup.ftp.upload_start");

		FTPClient client = new FTPClient();
		String server = ConfigHandler.ftpServer;
		int port = ConfigHandler.ftpPort;
		String username = ConfigHandler.ftpUsername;
		String password = ConfigHandler.ftpPassword;
		Path location = Paths.get(ConfigHandler.ftpLocation);

		try
		{
			LogHelper.info("Connecting to FTP Server %s:%d", server, port);
			client.connect(server, port);

			LogHelper.info("Authenticating with credentials.");
			if (client.login(username, password))
			{
				LogHelper.info("Logged in.");
				LogHelper.info("Entering passive mode.");
				client.enterLocalPassiveMode();
				client.setFileType(FTPClient.BINARY_FILE_TYPE);

				LogHelper.info("Starting upload.");
				InputStream input = new FileInputStream(target_file.toFile());
				boolean success = client.storeFile(location.resolve(target_file.getFileName()).toString(), input);

				input.close();

				if (success)
				{
					LogHelper.info("FTP upload finished successfully.");
					ChatHelper.sendLocalizedServerChatMsg("yabm2.backup.ftp.upload_success");
				}
				else
				{
					LogHelper.warn("FTP upload failed.");
					ChatHelper.sendLocalizedServerChatMsg("yabm2.backup.error.ftp_upload_failed");

					String[] replies = client.getReplyStrings();

					for (String reply : replies)
					{
						LogHelper.warn("FTP server reply: %s", reply);
					}
				}
			}
			else
			{
				LogHelper.error("FTP authentication failed.");
			}
		}
		catch (IOException ex)
		{
			LogHelper.error("Error during FTP upload: %s", ex.getMessage());
			ChatHelper.sendLocalizedServerChatMsg("yabm2.backup.error.ftp_upload_error", ex.getMessage());
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				if (client.isConnected())
				{
					client.logout();
					client.disconnect();
				}
			}
			catch (IOException ex)
			{
				LogHelper.error("Error while closing FTP connection: %s", ex.getMessage());
				ex.printStackTrace();
			}
		}
	}
}
