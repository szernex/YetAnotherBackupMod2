package org.szernex.yabm;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.szernex.yabm.handler.ConfigHandler;
import org.szernex.yabm.util.LogHelper;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, acceptableRemoteVersions = "*")
public class YABM
{
	@Mod.Instance(Reference.MOD_ID)
	public static YABM instance;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ConfigHandler.init(event.getSuggestedConfigurationFile());
		FMLCommonHandler.instance().bus().register(new ConfigHandler());
		LogHelper.debug("ConfigHandler registered");
	}
}
