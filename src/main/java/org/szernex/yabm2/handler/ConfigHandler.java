package org.szernex.yabm2.handler;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.Mod;
import net.minecraftforge.common.config.Configuration;
import org.szernex.yabm2.Reference;

import java.io.File;

public class ConfigHandler
{
	private static Configuration configuration;

	public static String[] blacklist =      new String[]{".*logs.*", ".*backups.*"};
	public static int backupInterval =      180;
	public static String[] backupSchedule = new String[]{};
	public static int compressionLevel =    9;
	public static String backupPrefix =     "backup";
	public static String backupPath =       "../backups";

	public static void init(File file)
	{
		if (configuration == null)
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

		blacklist = configuration.getStringList("blacklist", category, blacklist, "List of regular expressions to EXCLUDE from the backup.\nBy default everything in the installation directory and only the current save directory (in SP) will be included in the backup.\nUse this list to determine what should not be included.\nIf you only want to backup the current save set the blacklist to '.*' (without the quotes).");
		backupInterval = configuration.getInt("backupInterval", category, backupInterval, 0, Integer.MAX_VALUE, "Interval for backups in minutes. Useful for singleplayer.\nSet this to 0 to enable backupSchedule instead.");
		backupSchedule = configuration.getStringList("backupSchedule", category, backupSchedule, "The times for when to do backups in 24h format. Useful for servers.\nIf backupInterval is >0 then this setting has no effect.");
		compressionLevel = configuration.getInt("compressionLevel", category, compressionLevel, 0, 9, "The Zip compression level to use to create backup archives (0 = no compression, 9 = maximum compression).");
		backupPrefix = configuration.getString("backupPrefix", category, backupPrefix, "The prefix for the created Zip files. Archives will be formatted as '$prefix_worldname_timestamp.zip'.");
		backupPath = configuration.getString("backupPath", category, backupPath, "The path where to store the backups. Can be a relative or absolute path.");

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
