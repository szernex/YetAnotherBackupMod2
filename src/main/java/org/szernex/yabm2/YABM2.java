package org.szernex.yabm2;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import org.szernex.yabm2.command.CommandYABM;
import org.szernex.yabm2.core.BackupManager;
import org.szernex.yabm2.handler.ConfigHandler;
import org.szernex.yabm2.util.LogHelper;

import java.io.File;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, acceptableRemoteVersions = "*")
public class YABM2
{
	public static final BackupManager backupManager = new BackupManager();

	@Mod.Instance(Reference.MOD_ID)
	public static YABM2 instance;

	public static File configFile = null;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		configFile = event.getSuggestedConfigurationFile();
		ConfigHandler.init(configFile);
		FMLCommonHandler.instance().bus().register(new ConfigHandler());
		LogHelper.debug("ConfigHandler registered");
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandYABM());
	}

	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{
		backupManager.init();
		FMLCommonHandler.instance().bus().register(backupManager);
	}

	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent event)
	{
		FMLCommonHandler.instance().bus().unregister(backupManager);
		backupManager.stop();
	}
}
