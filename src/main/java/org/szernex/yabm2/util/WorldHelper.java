package org.szernex.yabm2.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;

import java.util.HashMap;
import java.util.Map;

public class WorldHelper
{
	private static HashMap<WorldServer, Boolean> saveFlags = null;

	public static void disableWorldSaving()
	{
		LogHelper.info("Saving worlds and turning world auto-save off");

		MinecraftServer server = MinecraftServer.getServer();
		ServerConfigurationManager server_config_manager = MinecraftServer.getServer().getConfigurationManager();
		int server_count = server.worldServers.length;
		saveFlags = new HashMap<>();

		server_config_manager.saveAllPlayerData();
		LogHelper.info("Player data saved");

		for (int i = 0; i < server_count; i++)
		{
			WorldServer world_server = server.worldServers[i];
			saveFlags.put(world_server, world_server.levelSaving);

			world_server.levelSaving = false;

			try
			{
				world_server.saveAllChunks(true, null);
				world_server.saveChunkData();

				LogHelper.debug("Saved world %s", world_server.toString());
			}
			catch (MinecraftException ex)
			{
				LogHelper.error("Error saving world %s: %s", world_server.toString(), ex.getMessage());
				ex.printStackTrace();
			}
		}

		LogHelper.info("Worlds saved");
		LogHelper.info("World auto-save turned off");
		ChatHelper.sendLocalizedServerChatMsg("yabm2.backup.general.autosave_off");
	}

	public static void enableWorldSaving()
	{
		if (saveFlags == null)
		{
			LogHelper.info("Save flags have not been set, world saving is probably not disabled, skipping");
			return;
		}

		LogHelper.info("Turning auto-save back on");

		for (Map.Entry<WorldServer, Boolean> entry : saveFlags.entrySet())
		{
			entry.getKey().levelSaving = entry.getValue();
			LogHelper.debug("Set level-saving for %s to %b", entry.getKey(), entry.getValue());
		}

		LogHelper.info("World auto-save turned on");
		ChatHelper.sendLocalizedServerChatMsg("yabm2.backup.general.autosave_on");
		saveFlags = null;
	}
}
