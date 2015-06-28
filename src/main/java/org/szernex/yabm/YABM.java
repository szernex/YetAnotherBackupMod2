package org.szernex.yabm;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, acceptableRemoteVersions = "*")
public class YABM
{
	@Mod.Instance(Reference.MOD_ID)
	public static YABM instance;

	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{

	}
}
