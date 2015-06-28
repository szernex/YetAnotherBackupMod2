package org.szernex.yabm.handler;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.Mod;
import net.minecraftforge.common.config.Configuration;
import org.szernex.yabm.Reference;

import java.io.File;

public class ConfigHandler
{
	private static Configuration configuration;

	public static String[] whitelist = new String[]{"*"};
	public static String[] blacklist = new String[]{"logs"};

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

		whitelist = configuration.getStringList("whitelist", category, whitelist, "List of all the files to INCLUDE in the backup.");
		blacklist = configuration.getStringList("blacklist", category, blacklist, "List of all the files to EXCLUDE from the backup. Overwrites the whitelist.");

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
