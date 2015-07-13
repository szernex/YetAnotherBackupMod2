package org.szernex.yabm.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;

import java.util.HashMap;
import java.util.Map;

public class WorldHelper
{
	public static HashMap<WorldServer, Boolean> disableWorldSaving()
	{
		LogHelper.info("Saving worlds and turning world auto-save off");

		MinecraftServer server = MinecraftServer.getServer();
		ServerConfigurationManager server_config_manager = MinecraftServer.getServer().getConfigurationManager();
		int server_count = server.worldServers.length;
		HashMap<WorldServer, Boolean> save_flags = new HashMap<>();

		server_config_manager.saveAllPlayerData();
		LogHelper.info("Player data saved");

		for (int i = 0; i < server_count; i++)
		{
			WorldServer world_server = server.worldServers[i];
			save_flags.put(world_server, world_server.levelSaving);

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

		return save_flags;
	}

	public static void enableWorldSaving(HashMap<WorldServer, Boolean> save_flags)
	{
		LogHelper.info("Turning auto-save back on");

		for (Map.Entry<WorldServer, Boolean> entry : save_flags.entrySet())
		{
			entry.getKey().levelSaving = entry.getValue();
			LogHelper.debug("Set level-saving for %s to %b", entry.getKey(), entry.getValue());
		}
	}
}
