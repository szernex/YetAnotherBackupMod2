package org.szernex.yabm;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import org.szernex.yabm.core.BackupManager;
import org.szernex.yabm.handler.ConfigHandler;
import org.szernex.yabm.util.LogHelper;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, acceptableRemoteVersions = "*")
public class YABM
{
	@Mod.Instance(Reference.MOD_ID)
	public static YABM instance;

	public BackupManager backupManager = new BackupManager();

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ConfigHandler.init(event.getSuggestedConfigurationFile());
		FMLCommonHandler.instance().bus().register(new ConfigHandler());
		LogHelper.debug("ConfigHandler registered");
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
		backupManager.stop();
	}
}
