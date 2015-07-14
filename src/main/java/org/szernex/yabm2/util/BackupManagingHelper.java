package org.szernex.yabm2.util;

import org.szernex.yabm2.handler.ConfigHandler;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;

public class BackupManagingHelper
{
	private static class BackupFileVisitor extends SimpleFileVisitor<Path>
	{
		private HashSet<Path> files = new HashSet<>();
		private PathMatcher matcher;

		public BackupFileVisitor(String pattern)
		{
			matcher = FileSystems.getDefault().getPathMatcher("regex:" + pattern);
		}

		public HashSet<Path> getFiles()
		{
			return files;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
		{
			if (matcher.matches(file.getFileName()))
				files.add(file);


			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
		{
			return FileVisitResult.SKIP_SUBTREE;
		}
	}

	public static boolean isPersistent()
	{
		Path backup_dir = Paths.get(ConfigHandler.backupPath).toAbsolutePath().normalize();
		Path persistent_dir = Paths.get(ConfigHandler.persistentPath).toAbsolutePath().normalize();
		String pattern = String.format("^%s%2$s.+%2$s(persistent%2$s)?.+\\.zip$", ConfigHandler.backupPrefix, BackupCreationHelper.FILENAME_SEPARATOR);
		BackupFileVisitor fileVisitor = new BackupFileVisitor(pattern);
		HashSet<Path> files = new HashSet<>();

		LogHelper.info("Checking directories for backups created today: %s %s", backup_dir, persistent_dir);
		LogHelper.info("Pattern used: " + pattern);

		try
		{
			Files.walkFileTree(backup_dir, fileVisitor);
			Files.walkFileTree(persistent_dir, fileVisitor);
			files.addAll(fileVisitor.getFiles());
		}
		catch (IOException ex)
		{
			LogHelper.error(ex.getMessage());
			ex.printStackTrace();
		}

		LocalDate today = LocalDate.now();

		for (Path file : files)
		{
			try
			{
				LocalDate last_modified = LocalDateTime.ofInstant(Files.getLastModifiedTime(file).toInstant(), ZoneId.systemDefault()).toLocalDate();

				if (today.equals(last_modified))
					return false;
			}
			catch (IOException ex)
			{
				LogHelper.warn("Could not read file attributes of %s: %s", file, ex.getMessage());
			}
		}

		return true;
	}
}
