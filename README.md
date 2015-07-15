# YetAnotherBackupMod2
*This is a complete re-work of the original <a href="https://github.com/szernex/YetAnotherBackupMod">YetAnotherBackupMod</a> Mod for Minecraft Forge 1.7.10.*

## Important note for users of YetAnotherBackupMod v1.x
YABM2 is a completely separate mod from YABM, therefor you will have to **remove YABM v1.x** before adding YABM2, otherwise you will end up with 2 mods making backups at possibly the same time!

You also need to have **Java 8** installed in order to use this version of the mod.

## What is YABM(2)?
YABM, or YetAnotherBackupMod, is a <a href="https://minecraft.net/">Minecraft</a> mod using the <a href="http://files.minecraftforge.net/">Forge framework</a>. It's only purpose is to provide a flexible yet easy to use way to plan backups of your Minecraft instance and is meant for both dedicated servers and SinglePlayer. Players on a server don't need to have YABM installed on their clients since it's a mod that just adds functionality (no blocks or items). Which means that if you are hosting a dedicated server using a pre-made ModPack you can still simply add YABM to your mods and your players don't have to do anything.

While I'm always trying my hardest to make this mod as reliable as possible it's always possible for bugs to occur, so please regurarily check your backups for integrity and report any bugs you find to me!

## YABM v1 vs v2
This is a list of most of the changes that happened between version 1 and 2, most of it are (hopefully) performance improvements and some changes on how to interact with the mod:
* By default YABM2 includes everything that is located in the installation directory of your Minecraft instance. The only exception are other world saves besides the one currently being played in (only applies to SinglePlayer).
* You can add regular expressions to the blacklist to control what should be excluded from the backup. If you want for instance to only backup the current world save you would just have to add `.*` to the blacklist setting.
* There are no longer getter/setter commands to manipulate config settings during runtime. Instead there is a `reloadconfig` command that lets you reload the whole configuration after changing and saving it.

Behind the scenes a lot of things have changed too, but those are not that important to the normal user:
* YABM2 is now using only the java.nio and java.time packages wherever possible since they provide more flexibility than what was used previously.
* The way to look for persistent backups and backups that should be consolidated has been completely reworked (now using regular expressions to precisely match files, using the actual file modification date instead of just filename).
* The whole threading has been reworked and hopefully optimized. World auto-save is now being turned off in the main thread instead of the backup thread which used to sometimes cause ConcurrentModificationExceptions in previous versions.
* YABM2 is now a bit more conscious about verbosing things, ie console spam should be less (most outputs are on DEBUG level now) and chat messages have been reduced to a minimum while still being informative.
 
### Functionality still missing from v1 (TBD)
* FTP upload to remote locations
* Config menu (for SinglePlayer)
