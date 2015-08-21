package org.szernex.yabm2.handler;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.Mod;
import net.minecraftforge.common.config.Configuration;
import org.szernex.yabm2.Reference;

import java.io.File;

public class ConfigHandler
{
	private static Configuration configuration;

	public static String[] blacklist =          new String[]{".*logs.*", ".*backups.*"};
	public static int backupInterval =          180;
	public static String[] backupSchedule =     new String[]{};
	public static int compressionLevel =        4;
	public static String backupPrefix =         "backup";
	public static String backupPath =           "../backups";
	public static boolean persistentBackups =   true;
	public static String persistentPath =       "../backups/persistent";
	public static int maxBackupCount =          8;
	public static int maxPersistentCount =      14;
	public static boolean ftpEnabled =          false;
	public static String ftpUsername =          "";
	public static String ftpPassword =          "";
	public static String ftpServer =            "";
	public static int ftpPort =                 21;
	public static String ftpLocation =          "yabm2_backups";
	public static boolean ftpPersistentOnly =   true;
	public static int pauseInterval =           0;
	public static int pauseDuration =           3;

	public static void init(File file)
	{
		if (file != null)
		{
			configuration = new Configuration(file);

			loadConfig();
		}
	}

	public static void loadConfig()
	{
		if (configuration == null)
		{
			return;
		}

		String category = Configuration.CATEGORY_GENERAL;

		blacklist = configuration.getStringList("blacklist", category, blacklist, "List of regular expressions to determine what to EXCLUDE from the backup.\nBy default everything in the installation directory and only the current save directory (in SP) will be included in the backup.\nThis blacklist will not be applied to the current save directory.\nIf you want to backup only the current save set the blacklist to '.*' (without the quotes).");
		backupInterval = configuration.getInt("backupInterval", category, backupInterval, 0, Integer.MAX_VALUE, "Interval for backups in minutes. Useful for singleplayer.\nSet this to 0 to enable backupSchedule instead.");
		backupSchedule = configuration.getStringList("backupSchedule", category, backupSchedule, "The times for when to do backups in 24h format. Useful for servers.\nIf backupInterval is >0 then this setting has no effect.");
		compressionLevel = configuration.getInt("compressionLevel", category, compressionLevel, 0, 9, "The Zip compression level to use to create backup archives (0 = no compression, 9 = maximum compression).");
		backupPrefix = configuration.getString("backupPrefix", category, backupPrefix, "The prefix for the created Zip files. Archives will be formatted as '$prefix_[persistent_]worldname_timestamp.zip'.");
		backupPath = configuration.getString("backupPath", category, backupPath, "The path where to store the backups. Can be a relative or absolute path.");
		persistentBackups = configuration.getBoolean("persistentBackups", category, persistentBackups, "Enables persistent backups which are done once per day and are excluded from automatic consolidation if enabled.");
		persistentPath = configuration.getString("persistentPath", category, persistentPath, "Same as backupPath but for persistent backups.");
		maxBackupCount = configuration.getInt("maxBackupCount", category, maxBackupCount, 0, Integer.MAX_VALUE, "The number of normal backups to keep. If the total backup count exceeds this number the oldest backups will be deleted.\n0 = disabled.");
		maxPersistentCount = configuration.getInt("maxPersistentCount", category, maxPersistentCount, 1, Integer.MAX_VALUE, "The number of persistent backups to keep. If the total backup count exceeds this number the oldest backups will be deleted.\n0 = disabled.");
		ftpEnabled = configuration.getBoolean("ftpEnabled", category, ftpEnabled, "Whether FTP upload is enabled or not.");
		ftpUsername = configuration.getString("ftpUsername", category, ftpUsername, "The username to use to authenticate with the FTP server.");
		ftpPassword = configuration.getString("ftpPassword", category, ftpPassword, "The password to use to authenticate with the FTP server.");
		ftpServer = configuration.getString("ftpServer", category, ftpServer, "The FTP server address to upload backups to.");
		ftpPort = configuration.getInt("ftpPort", category, ftpPort, 1, Integer.MAX_VALUE, "The FTP server port to use.");
		ftpLocation = configuration.getString("ftpLocation", category, ftpLocation, "The remote directory to upload backups to.");
		ftpPersistentOnly = configuration.getBoolean("ftpPersistentOnly", category, ftpPersistentOnly, "Whether to only upload persistent backups or everything.\nIf persistentBackups is set to false regular backups will be uploaded instead.");
		pauseInterval = configuration.getInt("pauseInterval", category, pauseInterval, 0, Integer.MAX_VALUE, "How many seconds to wait between pausing the backup creation.\nSet to 0 to disable pauses.");
		pauseDuration = configuration.getInt("pauseDuration", category, pauseDuration, 1, 100, "How many seconds to pause backup creation. Has no effect if pauseInterval = 0.");

		if (configuration.hasChanged())
		{
			configuration.save();
		}
	}

	@Mod.EventHandler
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if (event.modID.equalsIgnoreCase(Reference.MOD_ID))
		{
			loadConfig();
		}
	}
}
