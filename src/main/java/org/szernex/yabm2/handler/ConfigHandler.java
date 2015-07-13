package org.szernex.yabm2.handler;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.Mod;
import net.minecraftforge.common.config.Configuration;
import org.szernex.yabm2.Reference;

import java.io.File;

public class ConfigHandler
{
	private static Configuration configuration;

	public static String[] blacklist =      new String[]{".*logs.*"};
	public static int backupInterval =      180;
	public static String[] backupSchedule = new String[]{};

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

		blacklist = configuration.getStringList("blacklist", category, blacklist, "List of regular expressions to EXCLUDE from the backup.\nBy default everything in the installation directory will be included in the backup.\nUse this list to determine what should not be included.");
		backupInterval = configuration.getInt("backupInterval", category, backupInterval, 0, Integer.MAX_VALUE, "Interval for backups in minutes. Useful for singleplayer.\nSet this to 0 to enable backupSchedule instead.");
		backupSchedule = configuration.getStringList("backupSchedule", category, backupSchedule, "The times for when to do backups in 24h format. Useful for servers.\nIf backupInterval is >0 then this setting has no effect.");

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
